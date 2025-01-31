package strategy.util.fight.factory;

import java.util.List;
import java.util.Map;

import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotEffect;
import hydra.model.BotItemReader;
import strategy.util.CharacterService;

public abstract class AbstractHPRecovery implements HPRecovery {

	protected final CharacterDAO characterDao;

	protected AbstractHPRecovery(CharacterDAO characterDao) {
		this.characterDao = characterDao;
	}

	@Override
	public final boolean restoreHP(Map<String, Integer> reservedItems) {
		BotCharacter character = characterDao.getCharacter();
		int hpToHeal = character.getMaxHp() - character.getHp();
		if (hpToHeal == 0) {
			return true;
		}
		if (hpToHeal <= 15) {
			return characterDao.rest().ok();
		}
		return restoreHP(hpToHeal, reservedItems, character);
	}

	protected abstract boolean restoreHP(int hpToHeal, Map<String, Integer> reservedItems, BotCharacter character);

	protected static List<RestoreStruct> getHealItems(Map<String, Integer> reservedItems, int characterLevel,
			ItemDAO itemDAO, List<? extends BotItemReader> sourceItems) {
		return sourceItems.stream()
				.filter(bii -> !reservedItems.containsKey(bii.getCode())
						&& characterLevel >= itemDAO.getItem(bii.getCode()).getLevel())
				.map(bii -> new RestoreStruct(itemDAO.getItem(bii.getCode()), bii.getQuantity())).filter(bid -> bid
						.itemDetails().getEffects().stream().anyMatch(bie -> BotEffect.HEAL.equals(bie.getName()))).sorted(AbstractHPRecovery::compareRestoreStruct)
				.toList();
	}

	protected static int getHealValue(RestoreStruct healItem) {
		return healItem.itemDetails().getEffects().stream().filter(bie -> BotEffect.HEAL.equals(bie.getName()))
				.findFirst().get().getValue();
	}

	protected int restoreHPWithFoodInInventory(int hpToHeal, BotCharacter character, Map<String, Integer> reservedItems,
			ItemDAO itemDAO, CharacterService characterService) {
		// Utilisation de la nourriture si possible en ignorant la nourriture réservée
		List<RestoreStruct> healItems = getHealItems(reservedItems, character.getLevel(), itemDAO,
				characterService.getInventoryIgnoreEmpty());

		for (RestoreStruct healItem : healItems) {
			int singleHeal = getHealValue(healItem);
			int quantity;
			if (singleHeal * healItem.quantity() <= hpToHeal) {
				quantity = healItem.quantity();
			} else {
				quantity = hpToHeal / singleHeal + 1;
			}
			if (!characterDao.use(healItem.itemDetails().getCode(), quantity).ok()) {
				break;
			}
			hpToHeal -= singleHeal * quantity;
			if (hpToHeal <= 0) {
				break;
			}
		}
		return hpToHeal;
	}

	private static int compareRestoreStruct(RestoreStruct restorestruct1, RestoreStruct restorestruct2) {
		return Integer.compare(restorestruct1.itemDetails().getLevel(), restorestruct2.itemDetails().getLevel());
	}
}
