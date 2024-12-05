package strategy.achiever.factory;

import java.util.List;
import java.util.Map;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.TaskDAO;
import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverList;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.GoalAchieverTwoStep;
import strategy.achiever.factory.goals.ItemGetBankGoalAchiever;
import strategy.achiever.factory.goals.TradeGoalAchiever;
import strategy.achiever.factory.util.Coordinate;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class DefaultItemTaskFactory implements ItemTaskFactory {

	protected final CharacterDAO characterDAO;
	protected final TaskDAO taskDao;
	protected final BankDAO bankDAO;
	protected final Map<String, ArtifactGoalAchiever> itemsGoals;
	protected final CharacterService characterService;
	protected final MoveService moveService;
	protected final GoalParameter goalParameter;

	public DefaultItemTaskFactory(CharacterDAO characterDAO, TaskDAO taskDao, BankDAO bankDAO,
			Map<String, ArtifactGoalAchiever> itemsGoals, CharacterService characterService, MoveService moveService, GoalParameter goalParameter) {
		this.characterDAO = characterDAO;
		this.taskDao = taskDao;
		this.bankDAO = bankDAO;
		this.itemsGoals = itemsGoals;
		this.characterService = characterService;
		this.moveService = moveService;
		this.goalParameter = goalParameter;
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total, List<Coordinate> taskMasterCoordinates) {
		ArtifactGoalAchiever itemsGoal = itemsGoals.get(code);
		if (itemsGoal != null) {
			GoalAchiever goalAchiever;
			GoalAchieverList goalAchieverList = new GoalAchieverList();
			int freeSpace = characterService.getFreeInventorySpace();
			if (freeSpace < total) {
				DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(
						bankDAO, moveService, characterService, goalParameter);
				goalAchiever = new GoalAchieverTwoStep(characterDAO, depositNoReservedItemGoalAchiever,
						goalAchieverList, true, false);
				BotCharacter character = characterDAO.getCharacter();
				freeSpace = character.getInventoryMaxItems();
			} else {
				goalAchiever = goalAchieverList;
			}

			BotCharacter character = characterDAO.getCharacter();
			// On prend au max 90% de l'inventaire
			freeSpace = Math.min(freeSpace, Math.round(character.getInventoryMaxItems() * 0.9f));
			if (total > freeSpace) {
				int nbPack = total / freeSpace;
				for (int i = 0; i < nbPack; i++) {
					ItemGetBankGoalAchiever itemGetBankGoalAchiever = new ItemGetBankGoalAchiever(bankDAO, code,
							moveService, characterService);
					itemGetBankGoalAchiever.setQuantity(freeSpace);
					goalAchieverList.add(itemGetBankGoalAchiever);
					goalAchieverList.add(new GoalAchieverLoop(itemsGoal, freeSpace));
					goalAchieverList.add(new TradeGoalAchiever(moveService, taskDao, taskMasterCoordinates, code, freeSpace));
				}
				int quantity = total % freeSpace;
				if (quantity > 0) {
					ItemGetBankGoalAchiever itemGetBankGoalAchiever = new ItemGetBankGoalAchiever(bankDAO, code,
							moveService, characterService);
					itemGetBankGoalAchiever.setQuantity(quantity);
					goalAchieverList.add(itemGetBankGoalAchiever);
					goalAchieverList.add(new GoalAchieverLoop(itemsGoal, quantity));
					goalAchieverList.add(new TradeGoalAchiever(moveService, taskDao, taskMasterCoordinates, code, quantity));
				}
			} else {
				ItemGetBankGoalAchiever itemGetBankGoalAchiever = new ItemGetBankGoalAchiever(bankDAO, code,
						moveService, characterService);
				itemGetBankGoalAchiever.setQuantity(total);
				goalAchieverList.add(itemGetBankGoalAchiever);
				goalAchieverList.add(new GoalAchieverLoop(itemsGoal, total));
				goalAchieverList.add(new TradeGoalAchiever(moveService, taskDao, taskMasterCoordinates, code, total));
			}
			return goalAchiever;
		}
		return null;
	}

}
