package strategy.achiever.factory.goals;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;

public class ForceExecuteGoalAchiever implements GoalAchiever {

	private final GoalAchiever subGoal;

	public ForceExecuteGoalAchiever(GoalAchiever subGoal) {
		this.subGoal = subGoal;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		subGoal.isRealisable(character);
		return true;
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		return subGoal.execute(reservedItems);
	}

	@Override
	public boolean isFinish() {
		return subGoal.isFinish();
	}

	@Override
	public void clear() {
		subGoal.clear();
	}

	@Override
	public void setRoot() {
		subGoal.setRoot();
	}

	@Override
	public void unsetRoot() {
		subGoal.unsetRoot();
	}
	
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("subGoal", subGoal);
		return builder.toString();
	}
}
