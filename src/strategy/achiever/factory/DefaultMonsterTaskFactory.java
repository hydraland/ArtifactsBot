package strategy.achiever.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.MonsterGoalAchiever;

public class DefaultMonsterTaskFactory implements MonsterTaskFactory {

	private final Map<String, MonsterGoalAchiever> monsterGoals;
	private final GoalFactoryCreator factoryCreator;

	public DefaultMonsterTaskFactory(List<MonsterGoalAchiever> monsterGoals, GoalFactoryCreator factoryCreator) {
		this.factoryCreator = factoryCreator;
		this.monsterGoals = monsterGoals.stream()
				.collect(Collectors.toMap(MonsterGoalAchiever::getMonsterCode, Function.identity()));
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total) {
		if (monsterGoals.containsKey(code)) {
			GoalAchiever depositNoReservedItemGoalAchiever = factoryCreator.createDepositNoReservedItemGoalAchiever();
			GoalAchiever goalAchiever = factoryCreator.createGoalAchieverTwoStep(depositNoReservedItemGoalAchiever,
					monsterGoals.get(code), true, true);

			return factoryCreator.createGoalAchieverLoop(goalAchiever, total, false);
		}
		return null;
	}
}
