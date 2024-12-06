package strategy.util.fight.factory;

import java.util.List;
import java.util.Map;

import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotEffect;
import strategy.util.CharacterService;
import strategy.util.fight.HPRecovery;
import strategy.util.fight.RestoreStruct;

public class HPRecoveryImpl implements HPRecovery {
	public final CharacterDAO characterDao;
	public final CharacterService characterService;
	public final ItemDAO itemDAO;

	public HPRecoveryImpl(CharacterDAO characterDao, ItemDAO itemDAO, CharacterService characterService) {
		this.characterDao = characterDao;
		this.itemDAO = itemDAO;
		this.characterService = characterService;
	}

	@Override
	public boolean restoreHP(Map<String, Integer> reservedItems) {
		BotCharacter character = characterDao.getCharacter();
		int hpToHeal = character.getMaxHp() - character.getHp();
		if (hpToHeal == 0) {
			return true;
		}
		// Utilisation de la nourriture si possible en ignorant la nourriture réservée
		List<RestoreStruct> healItems = characterService.getInventoryIgnoreEmpty().stream()
				.filter(bii -> !reservedItems.containsKey(bii.getCode())
						&& character.getLevel() >= itemDAO.getItem(bii.getCode()).getLevel())
				.map(bii -> new RestoreStruct(itemDAO.getItem(bii.getCode()), bii.getQuantity())).filter(bid -> bid
						.itemDetails().getEffects().stream().anyMatch(bie -> BotEffect.HEAL.equals(bie.getName())))
				.toList();

		for (RestoreStruct healItem : healItems) {
			int singleHeal = healItem.itemDetails().getEffects().stream()
					.filter(bie -> BotEffect.HEAL.equals(bie.getName())).findFirst().get().getValue();
			int quantity;
			if (singleHeal * healItem.quantity() <= hpToHeal) {
				quantity = healItem.quantity();
			} else {
				quantity = hpToHeal / singleHeal + 1;
			}
			if (!characterDao.use(healItem.itemDetails().getCode(), quantity).ok()) {
				return false;
			}
			hpToHeal -= singleHeal * quantity;
			if (hpToHeal <= 0) {
				return true;
			}
		}
		if (hpToHeal > 0) {
			return characterDao.rest().ok();
		}
		return true;
	}
}