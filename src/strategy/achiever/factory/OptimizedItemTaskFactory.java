package strategy.achiever.factory;

import java.util.List;
import java.util.Map;

import hydra.dao.CharacterDAO;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.ClearGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverList;
import strategy.achiever.factory.goals.OptimizeGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.util.CharacterService;

public class OptimizedItemTaskFactory extends DefaultItemTaskFactory {

	private final GoalAverageOptimizer goalAverageOptimizer;

	public OptimizedItemTaskFactory(CharacterDAO characterDAO, GoalFactoryCreator factoryCreator,
			Map<String, GoalAchieverInfo<ArtifactGoalAchiever>> itemsGoalsInfo, CharacterService characterService,
			GoalAverageOptimizer goalAverageOptimizer) {
		super(characterDAO, factoryCreator, itemsGoalsInfo, characterService);
		this.goalAverageOptimizer = goalAverageOptimizer;
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total, List<Coordinate> taskMasterCoordinates) {
		GoalAchieverInfo<ArtifactGoalAchiever> goalAchieverInfo = itemsGoalsInfo.get(code);
		ArtifactGoalAchiever itemsGoal = goalAchieverInfo == null ? null : goalAchieverInfo.getGoal();
		if (itemsGoal != null) {
			int optimValue = goalAverageOptimizer.optimize(itemsGoal, total, 0.9f);
			if (optimValue > 1) {
				GoalAchieverList goalAchieverList = factoryCreator.createGoalAchieverList();
				if (total > optimValue) {
					int nbPack = total / optimValue;
					for (int i = 0; i < nbPack; i++) {
						goalAchieverList.add(itemsGoal);
						goalAchieverList
								.add(factoryCreator.createTradeGoalAchiever(taskMasterCoordinates, code, optimValue));
						goalAchieverList.add(new ClearGoalAchiever(itemsGoal));
					}
					int quantity = total % optimValue;
					if (quantity > 0) {
						goalAchieverList.add(new OptimizeGoalAchiever(itemsGoal, goalAverageOptimizer, quantity, 0.9f));
						goalAchieverList.add(itemsGoal);
						goalAchieverList
								.add(factoryCreator.createTradeGoalAchiever(taskMasterCoordinates, code, quantity));
					}
				} else {
					goalAchieverList.add(itemsGoal);
					goalAchieverList.add(factoryCreator.createTradeGoalAchiever(taskMasterCoordinates, code, total));
				}

				GoalAchiever depositNoReservedItemGoalAchiever = factoryCreator
						.createDepositNoReservedItemGoalAchiever();
				return factoryCreator.createGoalAchieverTwoStep(depositNoReservedItemGoalAchiever, goalAchieverList,
						true, false);
			}
		}
		return super.createTaskGoalAchiever(code, total, taskMasterCoordinates);
	}
}
