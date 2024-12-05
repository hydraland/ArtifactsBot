package strategy.achiever.factory.goals;

import java.util.List;

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

	public MonsterTaskGoalAchiever(CharacterDAO characterDAO, TaskDAO taskDao, BankDAO bankDAO,
			List<Coordinate> coordinates, MoveService moveService, CharacterService characterService,
			GoalParameter parameter) {
		super(characterDAO, taskDao, bankDAO, coordinates, moveService, characterService, parameter);
	}

	protected GoalAchiever createTaskGoalAchiever(String code, int total) {
		return parameter.getMonsterTaskFactory().createTaskGoalAchiever(code, total);
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