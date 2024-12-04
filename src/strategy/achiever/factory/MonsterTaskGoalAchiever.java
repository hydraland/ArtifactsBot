package strategy.achiever.factory;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.TaskDAO;
import hydra.model.BotTaskType;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.util.Coordinate;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public final class MonsterTaskGoalAchiever extends AbstractTaskGoalAchiever {
	private final List<MonsterGoalAchiever> monsterGoals;

	public MonsterTaskGoalAchiever(CharacterDAO characterDAO, TaskDAO taskDao, BankDAO bankDAO,
			List<MonsterGoalAchiever> monsterGoals, List<Coordinate> coordinates, MoveService moveService,
			CharacterService characterService, GoalParameter parameter) {
		super(characterDAO, taskDao, bankDAO, coordinates, moveService, characterService, parameter);
		this.monsterGoals = monsterGoals;
	}

	protected GoalAchiever createTaskGoalAchiever(String code, int total) {
		Optional<MonsterGoalAchiever> monsterGoal = monsterGoals.stream()
				.filter(mga -> code.equals(mga.getMonsterCode())).findFirst();
		if (monsterGoal.isPresent()) {
			DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(
					bankDAO, moveService, characterService);
			GoalAchiever goalAchiever = new GoalAchieverTwoStep(characterDAO, depositNoReservedItemGoalAchiever,
					monsterGoal.get(), true, true);

			return new GoalAchieverLoop(goalAchiever, total);
		}
		return null;
	}

	@Override
	protected BotTaskType getTaskType() {
		return BotTaskType.MONSTERS;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}

}
