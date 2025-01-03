package strategy.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hydra.dao.BankDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotCraftSkill;
import hydra.model.BotInventoryItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;

public interface CharacterService {

	boolean isPossess(String code, BankDAO bankDAO);

	boolean isPossess(String code, int quantity, BankDAO bankDAO);

	int getFreeInventorySpace();

	int getUtilitySlotQuantity(BotCharacterInventorySlot slot);

	int getQuantityInInventory(String code);

	int getLevel(BotCraftSkill key);

	int getLevel(BotResourceSkill key);

	Map<BotItemType, List<BotItemInfo>> getEquipableCharacterEquipement(
			Map<String, Integer> reservedItems, boolean useUtility);

	static String getSlotValue(BotCharacter character, BotCharacterInventorySlot slot) {
		switch (slot) {
		case WEAPON:
			return character.getWeaponSlot();
		case AMULET:
			return character.getAmuletSlot();
		case ARTIFACT1:
			return character.getArtifact1Slot();
		case ARTIFACT2:
			return character.getArtifact2Slot();
		case ARTIFACT3:
			return character.getArtifact3Slot();
		case BODY_ARMOR:
			return character.getBodyArmorSlot();
		case BOOTS:
			return character.getBootsSlot();
		case UTILITY1:
			return character.getUtility1Slot();
		case UTILITY2:
			return character.getUtility2Slot();
		case HELMET:
			return character.getHelmetSlot();
		case LEG_ARMOR:
			return character.getLegArmorSlot();
		case RING1:
			return character.getRing1Slot();
		case RING2:
			return character.getRing2Slot();
		case SHIELD:
			return character.getShieldSlot();
		default:
			throw new IllegalArgumentException("Value  " + slot + " not authorize");
		}
	}

	int getCharacterHPWithoutEquipment();

	Map<BotItemType, List<BotItemInfo>> getEquipableCharacterEquipementInBank(BankDAO bankDAO,
			Map<String, Integer> reservedItems, boolean useUtility);

	int getQuantityEquipableForUtility(BotItemInfo botItemInfo, BotCharacterInventorySlot slot);

	boolean isInventorySlotFull();

	long getInventoryFreeSlotNumber();

	long getInventoryMaxSlot();

	int getInventoryItemNumber();

	List<String> getNoPotionEquipedItems();

	List<BotInventoryItem> getFilterEquipementInInventory(Collection<String> equipementNames,
			String excludeEquipementName);

	Optional<BotInventoryItem> getFirstEquipementInInventory(List<String> equipementNames);

	default boolean inventoryConstaints(BotItemDetails itemDetails, int number) {
		return inventoryConstaints(itemDetails.getCode(), number);
	}

	List<BotInventoryItem> getInventoryIgnoreEmpty();

	boolean isPossessAny(List<String> codes, BankDAO bankDAO);

	boolean inventoryConstaints(String code, int number);

	boolean isPossessOnSelf(String code);

}