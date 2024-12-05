package strategy.achiever.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.GoalAchieverTwoStep;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class DefaultMonsterTaskFactory implements MonsterTaskFactory {

	private final Map<String,  MonsterGoalAchiever> monsterGoals;
	private final BankDAO bankDAO;
	private final MoveService moveService;
	private final CharacterService characterService;
	private final CharacterDAO characterDAO;
	private final GoalParameter goalParameter;

	public DefaultMonsterTaskFactory(List<MonsterGoalAchiever> monsterGoals, BankDAO bankDAO, CharacterDAO characterDAO,
			MoveService moveService, CharacterService characterService, GoalParameter goalParameter) {
		this.goalParameter = goalParameter;
		this.monsterGoals = monsterGoals.stream().collect(Collectors.toMap(MonsterGoalAchiever::getMonsterCode, Function.identity()));
		this.bankDAO = bankDAO;
		this.characterDAO = characterDAO;
		this.moveService = moveService;
		this.characterService = characterService;
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total) {
		if (monsterGoals.containsKey(code)) {
			DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(
					bankDAO, moveService, characterService, goalParameter);
			GoalAchiever goalAchiever = new GoalAchieverTwoStep(characterDAO, depositNoReservedItemGoalAchiever,
					monsterGoals.get(code), true, true);

			return new GoalAchieverLoop(goalAchiever, total);
		}
		return null;
	}

}
