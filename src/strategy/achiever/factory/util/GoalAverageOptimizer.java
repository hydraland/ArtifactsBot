package strategy.achiever.factory.util;

import strategy.achiever.factory.goals.ArtifactGoalAchiever;

public interface GoalAverageOptimizer {
	int optimize(ArtifactGoalAchiever goalAchiever, int max, float inventoryPercentMaxUse);
}
