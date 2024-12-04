package strategy.achiever;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;

public class GoalAchieverConditional implements GoalAchiever {

	private GoalAchiever subGoal;
	private boolean finish;
	private boolean fictifRoot;
	private Condition condition;

	public GoalAchieverConditional(GoalAchiever subGoal, Condition condition) {
		this.subGoal = subGoal;
		this.condition = condition;
		this.finish = false;
	}

	public GoalAchieverConditional(GoalAchiever subGoal, Condition condition, boolean fictifRoot) {
		this.subGoal = subGoal;
		this.condition = condition;
		this.fictifRoot = fictifRoot;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return subGoal.isRealisable(character);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			while (!condition.isFinish()) {
				if (subGoal.execute(reservedItems)) {
					if (subGoal.isFinish()) {
						subGoal.clear();
					}
				} else {
					return false;
				}
			}
			return true;
		} finally {
			finish = true;
		}
	}

	@Override
	public boolean isFinish() {
		return finish;
	}

	@Override
	public void clear() {
		finish = false;
		subGoal.clear();
	}

	@Override
	public void setRoot() {
		if (fictifRoot) {
			subGoal.setRoot();
		} else {
			subGoal.unsetRoot();
		}
	}

	@Override
	public void unsetRoot() {
		subGoal.unsetRoot();
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("fictifRoot", fictifRoot);
		builder.append("subGoal", subGoal);
		return builder.toString();
	}

	public interface Condition {
		public boolean isFinish();
	}
}
