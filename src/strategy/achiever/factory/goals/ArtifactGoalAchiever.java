package strategy.achiever.factory.goals;

import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.util.Cumulator;

public interface ArtifactGoalAchiever extends GoalAchiever {
	double getRate();

	boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem);

	default boolean acceptAndSetMultiplierCoefficient() {
		return acceptAndSetMultiplierCoefficient(1);
	}

	default boolean acceptAndSetMultiplierCoefficient(int coefficient) {
		return acceptAndSetMultiplierCoefficient(coefficient, new Cumulator(0), Integer.MAX_VALUE);
	}
}
