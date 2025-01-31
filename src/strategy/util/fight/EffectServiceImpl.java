package strategy.util.fight;

import hydra.model.BotItemDetails;
import util.CacheManager;
import util.PermanentCacheManager;

public final class EffectServiceImpl implements EffectService {
	private final CacheManager<String, ItemEffects> effectCacheManager;

	public EffectServiceImpl() {
		this.effectCacheManager = new PermanentCacheManager<>();
	}

	@Override
	public void updateEffectsCumulator(EffectCumulator effectsCumulator, BotItemDetails botItemDetail, int quantity) {
		effectsCumulator.accumulate(getEffects(botItemDetail), quantity);
	}
	
	@Override
	public ItemEffects getEffects(BotItemDetails botItemDetail) {
		ItemEffects cacheEffectCumulator;
		if (effectCacheManager.contains(botItemDetail.getCode())) {
			cacheEffectCumulator = effectCacheManager.get(botItemDetail.getCode());
		} else {
			cacheEffectCumulator = new ItemEffectsImpl();
			botItemDetail.getEffects().stream().forEach(effect -> {
				cacheEffectCumulator.setEffectValue(effect.getName(), effect.getValue());
			});
			effectCacheManager.add(botItemDetail.getCode(), cacheEffectCumulator);
		}
		return cacheEffectCumulator;
	}
}
