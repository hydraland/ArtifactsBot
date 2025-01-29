package strategy.achiever.factory.goals;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import strategy.achiever.CheckRealisableGoalAchiever;
import strategy.achiever.GoalAchiever;

public final class GoalAchieverForLoop implements GoalAchiever {
	private int quantity;
	private final GoalAchiever subGoal;
	private boolean finish;

	public GoalAchieverForLoop(GoalAchiever subGoal, int quantity) {
		this.subGoal = subGoal;
		this.quantity = quantity;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return subGoal.isRealisable(character);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			for (int i = 0; i < quantity; i++) {
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
		subGoal.unsetRoot();
	}

	@Override
	public void unsetRoot() {
		subGoal.unsetRoot();
	}

	final CheckRealisableGoalAchiever getSubGoal() {
		return subGoal;
	}

	final int getQuantity() {
		return quantity;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("quantity", quantity);
		builder.append("subGoal", subGoal);
		return builder.toString();
	}
}
