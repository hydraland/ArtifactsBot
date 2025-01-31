package strategy.util.fight;

import hydra.model.BotItemDetails;

public interface EffectService {
	void updateEffectsCumulator(EffectCumulator effectsCumulator, BotItemDetails botItemDetail, int quantity);

	ItemEffects getEffects(BotItemDetails botItemDetail);
}
