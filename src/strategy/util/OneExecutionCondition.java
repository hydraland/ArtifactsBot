package strategy.util;

import strategy.achiever.GoalAchieverConditional.Condition;

public final class OneExecutionCondition implements Condition {
	private boolean first;
	public OneExecutionCondition() {
		first = true;
	}
	@Override
	public boolean isFinish() {
		if(first) {
			first = false;
			return false;
		}
		return true;
	}
	
}