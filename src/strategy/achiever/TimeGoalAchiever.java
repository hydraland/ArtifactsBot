package strategy.achiever;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import strategy.util.AverageTimeXpCalculator;

public class TimeGoalAchiever implements GoalAchiever {

	private GoalAchiever subGoal;
	private boolean finish;
	private boolean fictifRoot;
	private XpGetter xpGetter;
	private AverageTimeXpCalculator averageTimeXpCalculator;

	public TimeGoalAchiever(GoalAchiever subGoal, XpGetter xpGetter, AverageTimeXpCalculator averageTimeXpCalculator) {
		this.subGoal = subGoal;
		this.xpGetter = xpGetter;
		this.averageTimeXpCalculator = averageTimeXpCalculator;
		this.finish = false;
	}

	public TimeGoalAchiever(GoalAchiever subGoal, XpGetter xpGetter, AverageTimeXpCalculator averageTimeXpCalculator,
			boolean fictifRoot) {
		this(subGoal, xpGetter, averageTimeXpCalculator);
		this.fictifRoot = fictifRoot;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return subGoal.isRealisable(character);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			int oldXP = xpGetter.getXp();
			long oldTime = System.currentTimeMillis();
			if (subGoal.execute(reservedItems)) {
				int newXP = xpGetter.getXp();
				long newTime = System.currentTimeMillis();
				averageTimeXpCalculator.add(newXP - oldXP, newTime - oldTime);
				return true;
			} else {
				return false;
			}
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

	public interface XpGetter {
		public int getXp();
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("fictifRoot", fictifRoot);
		builder.append("subGoal", subGoal);
		return builder.toString();
	}
}
