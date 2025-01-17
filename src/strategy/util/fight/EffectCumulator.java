package strategy.util.fight;

import hydra.model.BotEffect;

public interface EffectCumulator {
	void addEffectValue(BotEffect effect, int value);

	void addRestoreEffectValues(int value, int quantity);

	int getEffectValue(BotEffect effect);
	
	int getRestoreEffectValue(int quantity);
	
	void reset();
	
	boolean isUpper(EffectCumulator effectCumulator);

	boolean isRestore();
}
