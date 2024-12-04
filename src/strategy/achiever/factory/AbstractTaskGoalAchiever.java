package strategy.achiever.factory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.TaskDAO;
import hydra.dao.response.NewTaskResponse;
import hydra.dao.response.TaskRewardResponse;
import hydra.model.BotCharacter;
import hydra.model.BotInventoryItem;
import hydra.model.BotTaskType;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.util.Coordinate;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public abstract class AbstractTaskGoalAchiever implements GoalAchiever {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	protected final CharacterDAO characterDAO;
	protected final TaskDAO taskDao;
	protected final List<Coordinate> coordinates;
	protected boolean finish;
	protected GoalAchiever subGoal;
	protected final MoveService moveService;
	protected final BankDAO bankDAO;
	protected final GoalParameter parameter;
	protected final CharacterService characterService;

	protected AbstractTaskGoalAchiever(CharacterDAO characterDAO, TaskDAO taskDao, BankDAO bankDAO,
			List<Coordinate> coordinates, MoveService moveService, CharacterService characterService, GoalParameter parameter) {
		this.characterDAO = characterDAO;
		this.taskDao = taskDao;
		this.bankDAO = bankDAO;
		this.coordinates = coordinates;
		this.moveService = moveService;
		this.characterService = characterService;
		this.parameter = parameter;
		finish = false;
	}

	@Override
	public final boolean execute(Map<String, Integer> reservedItems) {
		try {
			BotCharacter character = characterDAO.getCharacter();
			if (character.getTaskType() == null) {
				if (moveService.moveTo(coordinates)) {
					NewTaskResponse response = taskDao.newTask();
					if (!response.ok()) {
						return false;
					}
					subGoal = createTaskGoalAchiever(response.task().getCode(), response.task().getTotal());
				} else {
					return false;
				}
			} else if (getTaskType().equals(character.getTaskType())
					&& character.getTaskTotal() > character.getTaskProgress()) {
				subGoal = createTaskGoalAchiever(character.getTask(),
						character.getTaskTotal() - character.getTaskProgress());
			} else if (!getTaskType().equals(character.getTaskType())) {
				return true;// Autre tache en cours
			}

			if (subGoal != null) {
				// Tache non terminée
				LOGGER.info("Execute Task Goal");
				LOGGER.info(subGoal.toString());
				if (subGoal.isRealisableAfterSetRoot(character)) {
					LOGGER.info("Goal realisable");
					subGoal.clear();
					if (!subGoal.execute(reservedItems)) {
						LOGGER.info("Goal fail");
						return false;
					}
					LOGGER.info("Goal succes");
				} else {
					boolean cancelAvailable = false;
					Optional<BotInventoryItem> firstEquipementInInventory = characterService
							.getFirstEquipementInInventory(Arrays.asList(GameConstants.COIN_CODE));
					if (firstEquipementInInventory.isPresent()) {
						cancelAvailable = true;
					} else if (bankDAO.getItem(GameConstants.COIN_CODE).getQuantity() > 0
							&& moveService.moveToBank()) {
						cancelAvailable = bankDAO.withdraw(bankDAO.getItem(GameConstants.COIN_CODE));
					}
					if (cancelAvailable) {
						return moveService.moveTo(coordinates) && taskDao.cancelTask().ok();
					}
					return true;
				}
			}

			// Chercher la récompense
			if (moveService.moveTo(coordinates)) {
				TaskRewardResponse response = taskDao.completeTask();
				if (response.ok()) {
					Optional<BotInventoryItem> firstEquipementInInventory = characterService
							.getFirstEquipementInInventory(Arrays.asList(GameConstants.COIN_CODE));
					int quantityInBank = bankDAO.getItem(GameConstants.COIN_CODE).getQuantity();
					if (firstEquipementInInventory.isPresent() && (firstEquipementInInventory.get().getQuantity()
							+ quantityInBank) >= GameConstants.MIN_CURRENCY_EXCHANGE_VALUE
									+ parameter.getCoinReserve()) {
						if (quantityInBank > 0 && (!moveService.moveToBank()
								|| !bankDAO.withdraw(bankDAO.getItem(GameConstants.COIN_CODE)))) {
							return false;
						}
						return moveService.moveTo(coordinates) && taskDao.exchange().ok();
					}
					return true;
				}
			}
			return false;
		} finally {
			this.finish = true;
		}
	}

	protected abstract BotTaskType getTaskType();

	@Override
	public final boolean isRealisable(BotCharacter character) {
		return true;
	}

	@Override
	public final boolean isFinish() {
		return finish;
	}

	@Override
	public final void clear() {
		finish = false;
		subGoal = null;
	}

	@Override
	public final void setRoot() {
	}

	@Override
	public final void unsetRoot() {
	}

	protected abstract GoalAchiever createTaskGoalAchiever(String code, int total);
}
