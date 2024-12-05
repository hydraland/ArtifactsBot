package strategy.achiever;

import hydra.model.BotCharacter;

public interface GoalAchiever extends CheckRealisableGoalAchiever, ExecutableGoalAchiever {
	boolean isFinish();
	void clear();
	void setRoot();
	void unsetRoot();
	default boolean isRealisableAfterSetRoot(BotCharacter character) {
		setRoot();
		return isRealisable(character);
	}
}
