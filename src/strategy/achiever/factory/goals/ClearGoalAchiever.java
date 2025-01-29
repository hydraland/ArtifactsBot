package strategy.achiever.factory.goals;

import java.util.Map;

import hydra.model.BotCharacter;
import strategy.achiever.factory.util.Cumulator;

public final class ClearGoalAchiever implements ArtifactGoalAchiever {

	private final ArtifactGoalAchiever itemsGoal;
	private boolean finish;

	public ClearGoalAchiever(ArtifactGoalAchiever itemsGoal) {
		this.itemsGoal = itemsGoal;
		finish = false;
	}
	
	@Override
	public boolean isRealisable(BotCharacter character) {
		return true;
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		itemsGoal.clear();
		finish = true;
		return true;
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
		return true;
	}

}
