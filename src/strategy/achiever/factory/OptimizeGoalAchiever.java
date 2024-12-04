package strategy.achiever.factory;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import strategy.achiever.factory.util.Cumulator;
import strategy.achiever.factory.util.GoalAverageOptimizer;

public class OptimizeGoalAchiever implements ArtifactGoalAchiever {

	private final ArtifactGoalAchiever itemsGoal;
	private final GoalAverageOptimizer goalAverageOptimizer;
	private final int max;
	private final float inventoryPercentMaxUse;
	private boolean finish;

	public OptimizeGoalAchiever(ArtifactGoalAchiever itemsGoal, GoalAverageOptimizer goalAverageOptimizer, int max,
			float inventoryPercentMaxUse) {
		this.itemsGoal = itemsGoal;
		this.goalAverageOptimizer = goalAverageOptimizer;
		this.max = max;
		this.inventoryPercentMaxUse = inventoryPercentMaxUse;
		finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return true;
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			return goalAverageOptimizer.optimize(itemsGoal, max, inventoryPercentMaxUse) == max;
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
	}

	@Override
	public void setRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	@Override
	public void unsetRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	@Override
	public double getRate() {
		return 1;
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		return false;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("max", max);
		builder.append("inventoryPercentMaxUse", inventoryPercentMaxUse);
		builder.append("itemsGoal", itemsGoal.getClass().getSimpleName());
		return builder.toString();
	}
}
