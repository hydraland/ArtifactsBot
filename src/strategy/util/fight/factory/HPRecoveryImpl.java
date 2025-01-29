package strategy.util.fight.factory;

import java.util.Map;

import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import strategy.util.CharacterService;

public final class HPRecoveryImpl extends AbstractHPRecovery {
	public final CharacterService characterService;
	public final ItemDAO itemDAO;

	public HPRecoveryImpl(CharacterDAO characterDao, ItemDAO itemDAO, CharacterService characterService) {
		super(characterDao);
		this.itemDAO = itemDAO;
		this.characterService = characterService;
	}

	@Override
	protected boolean restoreHP(int hpToHeal, Map<String, Integer> reservedItems, BotCharacter character) {
		int hpToHealAfterInventoryFood = restoreHPWithFoodInInventory(hpToHeal, character, reservedItems, itemDAO,
				characterService);
		if (hpToHealAfterInventoryFood <= 0) {
			return true;
		}

		if (hpToHeal > 0) {
			return characterDao.rest().ok();
		}
		return true;
	}
}