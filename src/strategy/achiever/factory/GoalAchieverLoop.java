package strategy.achiever.factory;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.GameConstants;
import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.util.Cumulator;
import util.BinomialProbability;

public class GoalAchieverLoop implements ArtifactGoalAchiever {
	private int quantity;
	private final GoalAchiever subGoal;
	private boolean finish;
	private boolean fictifRoot;
	private int coefficient;
	private int step;

	public GoalAchieverLoop(GoalAchiever subGoal, int quantity) {
		this.subGoal = subGoal;
		this.quantity = quantity;
		this.finish = false;
		this.coefficient = 1;
		this.step = 1;
	}

	public GoalAchieverLoop(GoalAchiever subGoal, int quantity, boolean fictifRoot) {
		this(subGoal, quantity);
		this.fictifRoot = fictifRoot;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return subGoal.isRealisable(character);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		int currentQuantity = quantity * coefficient;
		try {
			int current = 0;
			while (current < currentQuantity) {
				if (subGoal.execute(reservedItems)) {
					if (subGoal.isFinish()) {
						current += step;
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
	public double getRate() {
		return subGoal instanceof ArtifactGoalAchiever sg
				? BinomialProbability.calculateAtLeastN(GameConstants.MAX_TENTATIVE_FOR_RATE_CALCUL, quantity,
						sg.getRate())
				: 1;
	}

	final GoalAchiever getSubGoal() {
		return subGoal;
	}

	final int getQuantity() {
		return quantity;
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		int realQuantity = quantity * coefficient;
		this.coefficient = coefficient;
		step = 1;
		if (subGoal instanceof ArtifactGoalAchiever sg) {
			for (int testStep = coefficient; testStep > 0; testStep--) {
				Cumulator subCumulator = new Cumulator(cumulator.getValue() + realQuantity - testStep);
				if ((coefficient % testStep == 0)
						&& sg.acceptAndSetMultiplierCoefficient(testStep, subCumulator, maxItem)) {
					step = testStep;
					cumulator.addValue((realQuantity * subCumulator.getDiffValue()) / step);
					break;
				}
			}
		}
		return cumulator.getValue() <= maxItem;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("quantity", quantity);
		builder.append("fictifRoot", fictifRoot);
		builder.append("coefficient", coefficient);
		builder.append("step", step);
		builder.append("subGoal", subGoal);
		return builder.toString();
	}
}
