package hydra.dao.simulate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import hydra.GameConstants;
import hydra.dao.MapDAO;
import hydra.dao.TaskDAO;
import hydra.dao.response.CancelTaskResponse;
import hydra.dao.response.NewTaskResponse;
import hydra.dao.response.TaskRewardResponse;
import hydra.dao.response.TradeTaskResponse;
import hydra.model.BotBox;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotItem;
import hydra.model.BotResourceSkill;
import hydra.model.BotRewardDetail;
import hydra.model.BotRewards;
import hydra.model.BotTask;
import hydra.model.BotTaskDetail;
import hydra.model.BotTaskType;

public final class TaskDAOSimulator implements TaskDAO, Simulator<TaskStruct> {

	private static final String COMPLETE_TASK = "completeTask";
	private static final String CLASS_NAME = "TaskDAOSimulator";
	private TaskStruct taskStruct;
	private final FilteredInnerCallSimulatorListener simulatorListener;
	private final ByteArrayOutputStream memoryStream;
	private final CharacterDAOSimulator characterDAOSimulator;
	private final Random random;
	private final MapDAO mapDAO;

	public TaskDAOSimulator(FilteredInnerCallSimulatorListener simulatorListener,
			CharacterDAOSimulator characterDAOSimulator, MapDAO mapDAO) {
		this.simulatorListener = simulatorListener;
		this.characterDAOSimulator = characterDAOSimulator;
		this.mapDAO = mapDAO;
		memoryStream = new ByteArrayOutputStream();
		this.random = new Random();
	}

	@Override
	public NewTaskResponse newTask() {
		BotCharacter character = characterDAOSimulator.botCharacter;
		simulatorListener.startInnerCall();
		Optional<BotBox> searchTaskMaster = mapDAO.getTasksBox().stream()
				.filter(bb -> bb.getX() == character.getX() && bb.getY() == character.getY()).findFirst();
		simulatorListener.stopInnerCall();
		if (searchTaskMaster.isEmpty()) {
			simulatorListener.call(CLASS_NAME, "newTask", 0, true);
			return new NewTaskResponse(false, null);
		}
		List<BotTaskDetail> taskDetailFilteredList;
		if ("monsters".equals(searchTaskMaster.get().getContent().getCode())) {
			taskDetailFilteredList = getAllTask().stream()
					.filter(btd -> btd.getLevel() <= character.getLevel() && BotTaskType.MONSTERS.equals(btd.getType()))
					.toList();
		} else {
			taskDetailFilteredList = getAllTask().stream().filter(
					btd -> BotTaskType.ITEMS.equals(btd.getType()) && btd.getLevel() <= getSkillLevel(btd.getSkill()))
					.toList();
		}
		BotTaskDetail selectedTask = taskDetailFilteredList.get(random.nextInt(taskDetailFilteredList.size()));
		int quantity = random.nextInt(selectedTask.getMinQuantity(), selectedTask.getMaxQuantity() + 1);

		BotTask botTask = new BotTask();
		botTask.setCode(selectedTask.getCode());
		botTask.setTotal(quantity);
		botTask.setType(selectedTask.getType());

		character.setTask(selectedTask.getCode());
		character.setTaskProgress(0);
		character.setTaskTotal(quantity);
		character.setTaskType(selectedTask.getType());

		simulatorListener.call(CLASS_NAME, "newTask", 3, false);
		return new NewTaskResponse(true, botTask);
	}

	private int getSkillLevel(String skill) {
		int level;
		if (BotResourceSkill.ALCHEMY.name().equalsIgnoreCase(skill)
				|| BotResourceSkill.FISHING.name().equalsIgnoreCase(skill)
				|| BotResourceSkill.MINING.name().equalsIgnoreCase(skill)
				|| BotResourceSkill.WOODCUTTING.name().equalsIgnoreCase(skill)) {
			level = characterDAOSimulator.getCharacterService().getLevel(BotResourceSkill.valueOf(skill.toUpperCase()));
		} else {
			level = characterDAOSimulator.getCharacterService().getLevel(BotCraftSkill.valueOf(skill.toUpperCase()));
		}

		return level;
	}

	@Override
	public TaskRewardResponse exchange() {
		if (characterDAOSimulator.checkWithdrawInInventory(GameConstants.COIN_CODE,
				GameConstants.MIN_CURRENCY_EXCHANGE_VALUE)) {
			characterDAOSimulator.save(false);
			characterDAOSimulator.withdrawInInventory(GameConstants.COIN_CODE,
					GameConstants.MIN_CURRENCY_EXCHANGE_VALUE);
			double rateSum = getAllTaskReward().stream().map(brd -> 1d / brd.getRate()).reduce(Double::sum).get();
			double randomChooseTaskReward = random.nextDouble(rateSum);
			double cumul = 0;
			BotRewardDetail chooseTaskReward = null;
			for (BotRewardDetail taskReward : getAllTaskReward()) {
				cumul += 1d / taskReward.getRate();
				if (cumul >= randomChooseTaskReward) {
					chooseTaskReward = taskReward;
					break;
				}
			}
			BotRewards botRewards = new BotRewards();
			botRewards.setGold(0);
			List<BotItem> items = new ArrayList<>();
			BotItem item = new BotItem();
			item.setCode(chooseTaskReward.getCode());
			item.setQuantity(random.nextInt(chooseTaskReward.getMinQuantity(), chooseTaskReward.getMaxQuantity() + 1));
			botRewards.setItems(items);
			if (characterDAOSimulator.checkDepositInInventory(item.getCode(), item.getQuantity())) {
				characterDAOSimulator.depositInInventory(item.getCode(), item.getQuantity());
				simulatorListener.call(CLASS_NAME, "exchange", 3, false);
				return new TaskRewardResponse(true, botRewards);
			}
			characterDAOSimulator.load(false);
		}

		simulatorListener.call(CLASS_NAME, "exchange", 0, true);
		return new TaskRewardResponse(false, null);
	}

	@Override
	public TaskRewardResponse completeTask() {
		BotCharacter character = characterDAOSimulator.botCharacter;
		if (character.getTaskProgress() == character.getTaskTotal()) {
			BotTaskDetail botTaskDetail = getAllTask().stream().filter(btd -> btd.getCode().equals(character.getTask()))
					.findFirst().get();
			BotRewards botRewards = new BotRewards();
			botRewards.setGold(botTaskDetail.getRewards().getGold());
			List<BotItem> items = botTaskDetail.getRewards().getItems();
			botRewards.setItems(items);

			characterDAOSimulator.save(false);
			for (BotItem itemReceived : items) {
				if (characterDAOSimulator.checkDepositInInventory(itemReceived.getCode(), itemReceived.getQuantity())) {
					characterDAOSimulator.depositInInventory(itemReceived.getCode(), itemReceived.getQuantity());
				} else {
					// erreur, restauration de l'ancien perso
					characterDAOSimulator.load(false);
					simulatorListener.call(CLASS_NAME, COMPLETE_TASK, 0, true);
					return new TaskRewardResponse(false, null);
				}
			}

			resetTask(character);
			simulatorListener.call(CLASS_NAME, COMPLETE_TASK, 3, false);
			return new TaskRewardResponse(true, botRewards);
		}
		simulatorListener.call(CLASS_NAME, COMPLETE_TASK, 0, true);
		return new TaskRewardResponse(false, null);
	}

	private void resetTask(BotCharacter character) {
		character.setTask("");
		character.setTaskProgress(0);
		character.setTaskTotal(0);
		character.setTaskType(null);
	}

	@Override
	public TradeTaskResponse trade(String code, int quantity) {
		if (characterDAOSimulator.checkWithdrawInInventory(code, quantity)) {
			characterDAOSimulator.withdrawInInventory(code, quantity);
			BotCharacter character = characterDAOSimulator.botCharacter;
			character.setTaskProgress(character.getTaskProgress() - quantity);
			simulatorListener.call(CLASS_NAME, "trade", 3, false);
			return new TradeTaskResponse(true);
		}
		simulatorListener.call(CLASS_NAME, "trade", 0, true);
		return new TradeTaskResponse(false);
	}

	@Override
	public CancelTaskResponse cancelTask() {
		if (characterDAOSimulator.checkWithdrawInInventory(GameConstants.COIN_CODE, 1)) {
			characterDAOSimulator.withdrawInInventory(GameConstants.COIN_CODE, 1);
			BotCharacter character = characterDAOSimulator.botCharacter;
			resetTask(character);
			simulatorListener.call(CLASS_NAME, "cancelTask", 3, false);
			return new CancelTaskResponse(true);
		}
		simulatorListener.call(CLASS_NAME, "cancelTask", 0, true);
		return new CancelTaskResponse(false);
	}

	@Override
	public void load(boolean persistant) {
		taskStruct = Simulator.load(persistant, new File("TaskDAOSimulator.xml"), memoryStream);
		if (taskStruct == null) {
			taskStruct = new TaskStruct(Collections.emptyList(), Collections.emptyList());
		}
	}

	@Override
	public void save(boolean persistant) {
		Simulator.save(persistant, new File("TaskDAOSimulator.xml"), memoryStream, taskStruct);
	}

	@Override
	public List<BotTaskDetail> getAllTask() {
		return taskStruct.getBotTaskDetails();
	}

	@Override
	public List<BotRewardDetail> getAllTaskReward() {
		return taskStruct.getBotRewardDetails();
	}

	@Override
	public void set(TaskStruct value) {
		this.taskStruct = value;
	}
}
