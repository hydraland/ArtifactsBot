package strategy.achiever.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import hydra.dao.CharacterDAO;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverTwoStep;
import strategy.achiever.factory.goals.MonsterGoalAchiever;

public class DefaultMonsterTaskFactory implements MonsterTaskFactory {

	private final Map<String, MonsterGoalAchiever> monsterGoals;
	private final CharacterDAO characterDAO;
	private final GoalFactoryCreator factoryCreator;

	public DefaultMonsterTaskFactory(List<MonsterGoalAchiever> monsterGoals, CharacterDAO characterDAO,
			GoalFactoryCreator factoryCreator) {
		this.factoryCreator = factoryCreator;
		this.monsterGoals = monsterGoals.stream()
				.collect(Collectors.toMap(MonsterGoalAchiever::getMonsterCode, Function.identity()));
		this.characterDAO = characterDAO;
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total) {
		if (monsterGoals.containsKey(code)) {
			DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = factoryCreator
					.createDepositNoReservedItemGoalAchiever();
			GoalAchiever goalAchiever = new GoalAchieverTwoStep(characterDAO, depositNoReservedItemGoalAchiever,
					monsterGoals.get(code), true, true);

			return factoryCreator.createGoalAchieverLoop(goalAchiever, total);
		}
		return null;
	}
}
