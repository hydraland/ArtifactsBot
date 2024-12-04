package strategy.achiever;

import java.util.Map;

import hydra.model.BotCharacter;

public interface GoalAchiever {
	boolean isRealisable(BotCharacter character);
	boolean execute(Map<String, Integer> reservedItems);
	boolean isFinish();
	void clear();
	void setRoot();
	void unsetRoot();
	default boolean isRealisableAfterSetRoot(BotCharacter character) {
		setRoot();
		return isRealisable(character);
	}
}
