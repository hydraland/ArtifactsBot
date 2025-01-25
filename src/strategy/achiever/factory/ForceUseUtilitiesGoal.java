package strategy.achiever.factory;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;

final class ForceUseUtilitiesGoal implements GoalAchiever {

	private final GoalAchiever subGoal;
	private final GoalParameter goalParameter;
	private boolean activateUseUtilities;

	ForceUseUtilitiesGoal(GoalAchiever subGoal, GoalParameter goalParameter) {
		this.subGoal = subGoal;
		this.goalParameter = goalParameter;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		checkAndActivateUseUtilities();
		try {
			return subGoal.isRealisable(character);
		} finally {
			checkAndDesactivateUseUtilities();
		}
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		checkAndActivateUseUtilities();
		try {
			return subGoal.execute(reservedItems);
		} finally {
			checkAndDesactivateUseUtilities();
		}
	}
	
	private void checkAndActivateUseUtilities() {
		if (activateUseUtilities) {
			goalParameter.setForceUseUtilities(true);
		}
	}
	
	private void checkAndDesactivateUseUtilities() {
		if (activateUseUtilities) {
			goalParameter.setForceUseUtilities(false);
		}
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

	void forceUseUtilitiesState() {
		this.activateUseUtilities = true;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("subGoal", subGoal);
		return builder.toString();
	}
}