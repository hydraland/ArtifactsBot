package strategy.achiever.factory.util;

import strategy.achiever.factory.ArtifactGoalAchiever;

public interface GoalAverageOptimizer {
	int optimize(ArtifactGoalAchiever goalAchiever, int max, float inventoryPercentMaxUse);
}
