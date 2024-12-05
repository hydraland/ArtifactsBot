package strategy.achiever.factory;

import java.util.List;
import java.util.Map;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.TaskDAO;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.ClearGoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverList;
import strategy.achiever.factory.goals.GoalAchieverTwoStep;
import strategy.achiever.factory.goals.OptimizeGoalAchiever;
import strategy.achiever.factory.goals.TradeGoalAchiever;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class OptimizedItemTaskFactory extends DefaultItemTaskFactory {

	private final GoalAverageOptimizer goalAverageOptimizer;
	private final GoalParameter goalParameter;

	public OptimizedItemTaskFactory(CharacterDAO characterDAO, TaskDAO taskDao, BankDAO bankDAO,
			Map<String, ArtifactGoalAchiever> itemsGoals, CharacterService characterService, MoveService moveService,
			GoalAverageOptimizer goalAverageOptimizer, GoalParameter goalParameter) {
		super(characterDAO, taskDao, bankDAO, itemsGoals, characterService, moveService, goalParameter);
		this.goalAverageOptimizer = goalAverageOptimizer;
		this.goalParameter = goalParameter;
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total, List<Coordinate> taskMasterCoordinates) {
		ArtifactGoalAchiever itemsGoal = itemsGoals.get(code);
		if (itemsGoal != null) {
			int optimValue = goalAverageOptimizer.optimize(itemsGoal, total, 0.9f);
			if (optimValue > 1) {
				GoalAchieverList goalAchieverList = new GoalAchieverList();
				if (total > optimValue) {
					int nbPack = total / optimValue;
					for (int i = 0; i < nbPack; i++) {
						goalAchieverList.add(itemsGoal);
						goalAchieverList
								.add(new TradeGoalAchiever(moveService, taskDao, taskMasterCoordinates, code, optimValue));
						goalAchieverList.add(new ClearGoalAchiever(itemsGoal));
					}
					int quantity = total % optimValue;
					if (quantity > 0) {
						goalAchieverList.add(new OptimizeGoalAchiever(itemsGoal, goalAverageOptimizer, quantity, 0.9f));
						goalAchieverList.add(itemsGoal);
						goalAchieverList.add(new TradeGoalAchiever(moveService, taskDao, taskMasterCoordinates, code, quantity));
					}
				} else {
					goalAchieverList.add(itemsGoal);
					goalAchieverList.add(new TradeGoalAchiever(moveService, taskDao, taskMasterCoordinates, code, total));
				}

				DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(
						bankDAO, moveService, characterService, goalParameter);
				return new GoalAchieverTwoStep(characterDAO, depositNoReservedItemGoalAchiever, goalAchieverList, true,
						false);
			}
		}
		return super.createTaskGoalAchiever(code, total, taskMasterCoordinates);
	}
}
