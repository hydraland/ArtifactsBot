package strategy.achiever.factory;

import java.util.List;
import java.util.Map;

import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverList;
import strategy.achiever.factory.goals.GoalAchieverTwoStep;
import strategy.achiever.factory.goals.ItemGetBankGoalAchiever;
import strategy.achiever.factory.util.Coordinate;
import strategy.util.CharacterService;

public class DefaultItemTaskFactory implements ItemTaskFactory {

	protected final CharacterDAO characterDAO;
	protected final Map<String, ArtifactGoalAchiever> itemsGoals;
	protected final CharacterService characterService;
	protected final GoalFactoryCreator factoryCreator;

	public DefaultItemTaskFactory(CharacterDAO characterDAO, GoalFactoryCreator factoryCreator,
			Map<String, ArtifactGoalAchiever> itemsGoals, CharacterService characterService) {
		this.characterDAO = characterDAO;
		this.factoryCreator = factoryCreator;
		this.itemsGoals = itemsGoals;
		this.characterService = characterService;
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total, List<Coordinate> taskMasterCoordinates) {
		ArtifactGoalAchiever itemsGoal = itemsGoals.get(code);
		if (itemsGoal != null) {
			GoalAchiever goalAchiever;
			GoalAchieverList goalAchieverList = new GoalAchieverList();
			int freeSpace = characterService.getFreeInventorySpace();
			if (freeSpace < total) {
				DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = factoryCreator
						.createDepositNoReservedItemGoalAchiever();
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
					ItemGetBankGoalAchiever itemGetBankGoalAchiever = factoryCreator
							.createItemGetBankGoalAchiever(code);
					itemGetBankGoalAchiever.setQuantity(freeSpace);
					goalAchieverList.add(itemGetBankGoalAchiever);
					goalAchieverList.add(factoryCreator.createGoalAchieverLoop(itemsGoal, freeSpace));
					goalAchieverList
							.add(factoryCreator.createTradeGoalAchiever(taskMasterCoordinates, code, freeSpace));
				}
				int quantity = total % freeSpace;
				if (quantity > 0) {
					ItemGetBankGoalAchiever itemGetBankGoalAchiever = factoryCreator
							.createItemGetBankGoalAchiever(code);
					itemGetBankGoalAchiever.setQuantity(quantity);
					goalAchieverList.add(itemGetBankGoalAchiever);
					goalAchieverList.add(factoryCreator.createGoalAchieverLoop(itemsGoal, quantity));
					goalAchieverList.add(factoryCreator.createTradeGoalAchiever(taskMasterCoordinates, code, quantity));
				}
			} else {
				ItemGetBankGoalAchiever itemGetBankGoalAchiever = factoryCreator.createItemGetBankGoalAchiever(code);
				itemGetBankGoalAchiever.setQuantity(total);
				goalAchieverList.add(itemGetBankGoalAchiever);
				goalAchieverList.add(factoryCreator.createGoalAchieverLoop(itemsGoal, total));
				goalAchieverList.add(factoryCreator.createTradeGoalAchiever(taskMasterCoordinates, code, total));
			}
			return goalAchiever;
		}
		return null;
	}
}
