package strategy.achiever.factory.goals;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import strategy.achiever.CheckRealisableGoalAchiever;
import strategy.achiever.ExecutableGoalAchiever;
import strategy.achiever.GoalAchiever;

public final class GenericGoalAchiever implements GoalAchiever {

	private CheckRealisableGoalAchiever checkRealisableGoalAchiever;
	private ExecutableGoalAchiever executableGoalAchiever;
	private boolean finish;

	public GenericGoalAchiever(CheckRealisableGoalAchiever checkRealisableGoalAchiever,
			ExecutableGoalAchiever executableGoalAchiever) {
		this.checkRealisableGoalAchiever = checkRealisableGoalAchiever;
		this.executableGoalAchiever = executableGoalAchiever;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return checkRealisableGoalAchiever.isRealisable(character);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			return executableGoalAchiever.execute(reservedItems);
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

	public final void setCheckRealisableGoalAchiever(CheckRealisableGoalAchiever checkRealisableGoalAchiever) {
		this.checkRealisableGoalAchiever = checkRealisableGoalAchiever;
	}

	public final void setExecutableGoalAchiever(ExecutableGoalAchiever executableGoalAchiever) {
		this.executableGoalAchiever = executableGoalAchiever;
	}
	
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
