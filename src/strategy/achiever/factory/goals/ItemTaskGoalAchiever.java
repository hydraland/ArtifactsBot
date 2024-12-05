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

public final class ItemTaskGoalAchiever extends AbstractTaskGoalAchiever {

	public ItemTaskGoalAchiever(CharacterDAO characterDAO, TaskDAO taskDao, BankDAO bankDAO,
			List<Coordinate> coordinates, CharacterService characterService, MoveService moveService,
			GoalParameter parameter) {
		super(characterDAO, taskDao, bankDAO, coordinates, moveService, characterService, parameter);
	}

	@Override
	protected BotTaskType getTaskType() {
		return BotTaskType.ITEMS;
	}

	protected GoalAchiever createTaskGoalAchiever(String code, int total) {
		return parameter.getItemTaskFactory().createTaskGoalAchiever(code, total, coordinates);
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
