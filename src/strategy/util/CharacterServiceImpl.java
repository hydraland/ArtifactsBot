package strategy.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotCraftSkill;
import hydra.model.BotInventoryItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemReader;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;
import util.CacheManager;
import util.LimitedTimeCacheManager;

public final class CharacterServiceImpl implements CharacterService {
	private final CharacterDAO characterDao;
	private final ItemDAO itemDAO;
	private final CacheManager<String, Object> cache;

	public CharacterServiceImpl(CharacterDAO characterDao, ItemDAO itemDAO) {
		this.characterDao = characterDao;
		this.itemDAO = itemDAO;
		this.cache = new LimitedTimeCacheManager<>(86400);
	}

	@Override
	public boolean isPossess(String code, BankDAO bankDAO) {
		BotItemDetails itemDetail = new BotItemDetails();
		itemDetail.setCode(code);
		return inventoryConstaints(itemDetail, 1) || getNoPotionEquipedItems().contains(code)
				|| bankDAO.getItem(code).getQuantity() > 0;
	}

	@Override
	public boolean isPossessAny(List<String> codes, BankDAO bankDAO) {
		for (String code : codes) {
			BotItemDetails itemDetail = new BotItemDetails();
			itemDetail.setCode(code);
			boolean possesed = inventoryConstaints(itemDetail, 1) || getNoPotionEquipedItems().contains(code)
					|| bankDAO.getItem(code).getQuantity() > 0;
			if (possesed) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isPossess(String code, int quantity, BankDAO bankDAO) {
		Optional<BotInventoryItem> firstEquipementInInventory = getFirstEquipementInInventory(Arrays.asList(code));
		int nbInInventory = firstEquipementInInventory.isEmpty() ? 0 : firstEquipementInInventory.get().getQuantity();
		int nbInBank = bankDAO.getItem(code).getQuantity();
		long nbEquiped = getNoPotionEquipedItems().stream().filter(code::equals).count();
		return nbInInventory + nbInBank + nbEquiped >= quantity;
	}

	@Override
	public int getFreeInventorySpace() {
		BotCharacter character = characterDao.getCharacter();
		return character.getInventoryMaxItems() - getInventoryItemNumber();
	}

	@Override
	public int getQuantityEquipableForUtility(BotItemInfo botItemInfo, BotCharacterInventorySlot slot) {
		int freeQuantity = GameConstants.MAX_ITEM_IN_SLOT - getUtilitySlotQuantity(slot);
		return (Math.min(freeQuantity, botItemInfo.quantity()));
	}

	@Override
	public int getUtilitySlotQuantity(BotCharacterInventorySlot slot) {
		BotCharacter character = characterDao.getCharacter();
		return (slot.equals(BotCharacterInventorySlot.UTILITY1) ? character.getUtility1SlotQuantity()
				: character.getUtility2SlotQuantity());
	}

	@Override
	public int getQuantityInInventory(String code) {
		Optional<BotInventoryItem> invEqt = getFirstEquipementInInventory(Arrays.asList(code));
		if (invEqt.isPresent()) {
			return invEqt.get().getQuantity();
		}
		return 0;
	}

	@Override
	public int getLevel(BotCraftSkill key) {
		BotCharacter character = characterDao.getCharacter();
		switch (key) {
		case COOKING:
			return character.getCookingLevel();
		case GEARCRAFTING:
			return character.getGearcraftingLevel();
		case JEWELRYCRAFTING:
			return character.getJewelrycraftingLevel();
		case MINING:
			return character.getMiningLevel();
		case WEAPONCRAFTING:
			return character.getWeaponcraftingLevel();
		case WOODCUTTING:
			return character.getWoodcuttingLevel();
		case ALCHEMY:
			return character.getAlchemyLevel();
		default:
			throw new IllegalArgumentException("Unexpected value: " + key);
		}
	}

	@Override
	public int getLevel(BotResourceSkill key) {
		BotCharacter character = characterDao.getCharacter();
		switch (key) {
		case FISHING:
			return character.getFishingLevel();
		case WOODCUTTING:
			return character.getWoodcuttingLevel();
		case MINING:
			return character.getMiningLevel();
		case ALCHEMY:
			return character.getAlchemyLevel();
		default:
			throw new IllegalArgumentException("Unexpected value: " + key);
		}
	}

	@SuppressWarnings("unchecked")
	private List<BotItemInfo> getEquipableCharacterEquipement(BotItemType itemType, Map<String, Integer> reservedItems,
			SlotQuantityStruct... equipementSlots) {
		BotCharacter character = characterDao.getCharacter();
		Map<String, BotItemInfo> inventoryItemInfo = new HashMap<>();
		Map<String, BotItemDetails> equipementsMap;
		List<String> equipementsName;
		if (cache.contains("M" + itemType + character.getLevel())) {
			equipementsMap = (Map<String, BotItemDetails>) cache.get("M" + itemType + character.getLevel());
			equipementsName = (List<String>) cache.get("N" + itemType + character.getLevel());
		} else {
			List<BotItemDetails> equipements = itemDAO.getItems(itemType, 1, character.getLevel());
			equipementsMap = equipements.stream().collect(Collectors.toMap(item -> item.getCode(), item -> item));
			equipementsName = equipements.stream().map(item -> item.getCode()).toList();
			cache.add("M" + itemType + character.getLevel(), equipementsMap);
			cache.add("N" + itemType + character.getLevel(), equipementsName);
		}

		List<BotInventoryItem> inventoryEqtsFiltered = getFilterEquipementInInventory(equipementsName, "").stream()
				.filter(bii -> !reservedItems.containsKey(bii.getCode())).toList();
		List<BotItemInfo> equipementsCharacter = new ArrayList<>();
		equipementsCharacter.addAll(inventoryEqtsFiltered.stream().<BotItemInfo>mapMulti((item, u) -> {
			String code = item.getCode();
			if (equipementsMap.containsKey(code)) {
				BotItemInfo botItemInfo = new BotItemInfo(equipementsMap.get(code), item.getQuantity(),
						ItemOrigin.ON_SELF);
				u.accept(botItemInfo);
				inventoryItemInfo.put(code, botItemInfo);
			} // Sinon l'item est de trop haut niveau pour le perso
		}).toList());

		for (SlotQuantityStruct equipementSlot : equipementSlots) {
			String slotCode = equipementSlot.slot();
			if (!"".equals(slotCode)) {
				BotItemDetails botItemDetails = equipementsMap.get(slotCode);
				if (inventoryItemInfo.containsKey(slotCode)) {
					BotItemInfo oldBotItemInfo = inventoryItemInfo.get(slotCode);
					equipementsCharacter.remove(oldBotItemInfo);
					equipementsCharacter.add(new BotItemInfo(botItemDetails,
							equipementSlot.quantity() + oldBotItemInfo.quantity(), ItemOrigin.ON_SELF));
				} else {
					equipementsCharacter
							.add(new BotItemInfo(botItemDetails, equipementSlot.quantity(), ItemOrigin.ON_SELF));
				}
			}
		}

		return equipementsCharacter;
	}

	@Override
	public Map<BotCharacterInventorySlot, List<BotItemInfo>> getEquipableCharacterEquipement(
			Map<String, Integer> reservedItems, boolean useUtility) {
		BotCharacter character = characterDao.getCharacter();

		List<BotItemInfo> weaponCharacter = getEquipableCharacterEquipement(BotItemType.WEAPON, reservedItems,
				new SlotQuantityStruct(character.getWeaponSlot(), 1));
		List<BotItemInfo> bodyArmorCharacter = getEquipableCharacterEquipement(BotItemType.BODY_ARMOR, reservedItems,
				new SlotQuantityStruct(character.getBodyArmorSlot(), 1));
		List<BotItemInfo> bootsCharacter = getEquipableCharacterEquipement(BotItemType.BOOTS, reservedItems,
				new SlotQuantityStruct(character.getBootsSlot(), 1));
		List<BotItemInfo> helmetCharacter = getEquipableCharacterEquipement(BotItemType.HELMET, reservedItems,
				new SlotQuantityStruct(character.getHelmetSlot(), 1));
		List<BotItemInfo> shieldCharacter = getEquipableCharacterEquipement(BotItemType.SHIELD, reservedItems,
				new SlotQuantityStruct(character.getShieldSlot(), 1));
		List<BotItemInfo> legArmorCharacter = getEquipableCharacterEquipement(BotItemType.LEG_ARMOR, reservedItems,
				new SlotQuantityStruct(character.getLegArmorSlot(), 1));
		List<BotItemInfo> amuletCharacter = getEquipableCharacterEquipement(BotItemType.AMULET, reservedItems,
				new SlotQuantityStruct(character.getAmuletSlot(), 1));
		List<BotItemInfo> ring1Character = getEquipableCharacterEquipement(BotItemType.RING, reservedItems,
				new SlotQuantityStruct(character.getRing1Slot(), 1),
				new SlotQuantityStruct(character.getRing2Slot(), 1));
		List<BotItemInfo> ring2Character = new ArrayList<>(ring1Character);
		List<BotItemInfo> conso1Character;
		if (useUtility) {
			conso1Character = getEquipableCharacterEquipement(BotItemType.UTILITY, reservedItems,
					new SlotQuantityStruct(character.getUtility1Slot(), character.getUtility1SlotQuantity()),
					new SlotQuantityStruct(character.getUtility2Slot(), character.getUtility2SlotQuantity()));
		} else {
			conso1Character = new ArrayList<>();
		}
		List<BotItemInfo> conso2Character = new ArrayList<>(conso1Character);
		List<BotItemInfo> artifact1Character = getEquipableCharacterEquipement(BotItemType.ARTIFACT, reservedItems,
				new SlotQuantityStruct(character.getArtifact1Slot(), 1),
				new SlotQuantityStruct(character.getArtifact2Slot(), 1),
				new SlotQuantityStruct(character.getArtifact3Slot(), 1));
		List<BotItemInfo> artifact2Character = new ArrayList<>(artifact1Character);
		List<BotItemInfo> artifact3Character = new ArrayList<>(artifact1Character);

		Map<BotCharacterInventorySlot, List<BotItemInfo>> result = new EnumMap<>(BotCharacterInventorySlot.class);
		result.put(BotCharacterInventorySlot.WEAPON, weaponCharacter);
		result.put(BotCharacterInventorySlot.BODY_ARMOR, bodyArmorCharacter);
		result.put(BotCharacterInventorySlot.BOOTS, bootsCharacter);
		result.put(BotCharacterInventorySlot.HELMET, helmetCharacter);
		result.put(BotCharacterInventorySlot.SHIELD, shieldCharacter);
		result.put(BotCharacterInventorySlot.LEG_ARMOR, legArmorCharacter);
		result.put(BotCharacterInventorySlot.AMULET, amuletCharacter);
		result.put(BotCharacterInventorySlot.RING1, ring1Character);
		result.put(BotCharacterInventorySlot.RING2, ring2Character);
		result.put(BotCharacterInventorySlot.UTILITY1, conso1Character);
		result.put(BotCharacterInventorySlot.UTILITY2, conso2Character);
		result.put(BotCharacterInventorySlot.ARTIFACT1, artifact1Character);
		result.put(BotCharacterInventorySlot.ARTIFACT2, artifact2Character);
		result.put(BotCharacterInventorySlot.ARTIFACT3, artifact3Character);

		return result;
	}

	@Override
	public int getCharacterHPWithoutEquipment() {
		BotCharacter character = characterDao.getCharacter();
		return GameConstants.HP_LEVEL_1 + GameConstants.HP_PER_LEVEL * character.getLevel();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<BotCharacterInventorySlot, List<BotItemInfo>> getEquipableCharacterEquipementInBank(BankDAO bankDAO,
			Map<String, Integer> reservedItems, boolean useUtility) {
		BotCharacter character = characterDao.getCharacter();
		Map<String, BotItemDetails> equipementsMap;
		if (cache.contains("M" + character.getLevel())) {
			equipementsMap = (Map<String, BotItemDetails>) cache.get("M" + character.getLevel());
		} else {
			List<BotItemDetails> equipements = itemDAO.getItems(1, character.getLevel());
			equipementsMap = equipements.stream().collect(Collectors.toMap(item -> item.getCode(), item -> item));
			cache.add("M" + character.getLevel(), equipementsMap);
		}

		Map<BotCharacterInventorySlot, List<BotItemInfo>> result = new EnumMap<>(BotCharacterInventorySlot.class);
		List<? extends BotItemReader> itemInBankFiltered = bankDAO.viewItems().stream()
				.filter(bi -> !reservedItems.containsKey(bi.getCode())).toList();
		for (BotItemReader item : itemInBankFiltered) {
			BotItemDetails botItemDetails = equipementsMap.get(item.getCode());
			if (botItemDetails != null && !BotItemType.RESOURCE.equals(botItemDetails.getType())
					&& !BotItemType.CURRENCY.equals(botItemDetails.getType())
					&& !BotItemType.CONSUMABLE.equals(botItemDetails.getType())) {
				BotItemType type = botItemDetails.getType();
				switch (type) {
				case WEAPON: {
					List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.WEAPON,
							k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					break;
				}
				case AMULET: {
					List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.AMULET,
							k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					break;
				}
				case BODY_ARMOR: {
					List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.BODY_ARMOR,
							k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					break;
				}
				case BOOTS: {
					List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.BOOTS,
							k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					break;
				}
				case HELMET: {
					List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.HELMET,
							k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					break;
				}
				case LEG_ARMOR: {
					List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.LEG_ARMOR,
							k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					break;
				}
				case SHIELD: {
					List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.SHIELD,
							k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					break;
				}
				case UTILITY: {
					if (useUtility) {
						List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.UTILITY1,
								k -> new ArrayList<>());
						listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
						listItems = result.computeIfAbsent(BotCharacterInventorySlot.UTILITY2, k -> new ArrayList<>());
						listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					}
					break;
				}
				case RING: {
					List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.RING1,
							k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					listItems = result.computeIfAbsent(BotCharacterInventorySlot.RING2, k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					break;
				}
				case ARTIFACT: {
					List<BotItemInfo> listItems = result.computeIfAbsent(BotCharacterInventorySlot.ARTIFACT1,
							k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					listItems = result.computeIfAbsent(BotCharacterInventorySlot.ARTIFACT2, k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					listItems = result.computeIfAbsent(BotCharacterInventorySlot.ARTIFACT3, k -> new ArrayList<>());
					listItems.add(new BotItemInfo(botItemDetails, item.getQuantity(), ItemOrigin.BANK));
					break;
				}
				default:
					throw new IllegalArgumentException("Value  " + type + " not authorize");
				}
			}

		}
		return result;
	}

	public boolean isInventoryFull() {
		BotCharacter character = characterDao.getCharacter();
		return character.getInventoryMaxItems() == getInventoryItemNumber();
	}

	@Override
	public boolean isInventorySlotFull() {
		return getInventoryFreeSlotNumber() == 0;
	}

	@Override
	public long getInventoryFreeSlotNumber() {
		BotCharacter character = characterDao.getCharacter();
		return character.getInventory().stream().filter(item -> item.getQuantity() == 0).count();
	}

	@Override
	public long getInventoryMaxSlot() {
		BotCharacter character = characterDao.getCharacter();
		return character.getInventory().size();
	}

	@Override
	public int getInventoryItemNumber() {
		BotCharacter character = characterDao.getCharacter();
		return character.getInventory().stream().collect(Collectors.summingInt(BotItemReader::getQuantity));
	}

	@Override
	public List<String> getNoPotionEquipedItems() {
		BotCharacter character = characterDao.getCharacter();
		ArrayList<String> equipedEquipement = new ArrayList<>();
		if (!"".equals(character.getWeaponSlot())) {
			equipedEquipement.add(character.getWeaponSlot());
		}
		if (!"".equals(character.getBodyArmorSlot())) {
			equipedEquipement.add(character.getBodyArmorSlot());
		}
		if (!"".equals(character.getHelmetSlot())) {
			equipedEquipement.add(character.getHelmetSlot());
		}
		if (!"".equals(character.getShieldSlot())) {
			equipedEquipement.add(character.getShieldSlot());
		}
		if (!"".equals(character.getLegArmorSlot())) {
			equipedEquipement.add(character.getLegArmorSlot());
		}
		if (!"".equals(character.getBootsSlot())) {
			equipedEquipement.add(character.getBootsSlot());
		}
		if (!"".equals(character.getAmuletSlot())) {
			equipedEquipement.add(character.getAmuletSlot());
		}
		if (!"".equals(character.getRing1Slot())) {
			equipedEquipement.add(character.getRing1Slot());
		}
		if (!"".equals(character.getRing2Slot())) {
			equipedEquipement.add(character.getRing2Slot());
		}
		if (!"".equals(character.getArtifact1Slot())) {
			equipedEquipement.add(character.getArtifact1Slot());
		}
		if (!"".equals(character.getArtifact2Slot())) {
			equipedEquipement.add(character.getArtifact2Slot());
		}
		if (!"".equals(character.getArtifact3Slot())) {
			equipedEquipement.add(character.getArtifact3Slot());
		}
		return equipedEquipement;
	}

	@Override
	public boolean isPossessOnSelf(String code) {
		BotCharacter character = characterDao.getCharacter();
		return code.equals(character.getUtility1Slot()) || code.equals(character.getUtility2Slot())
				|| getNoPotionEquipedItems().contains(code) || inventoryConstaints(code, 1);
	}

	@Override
	public List<BotInventoryItem> getInventoryIgnoreEmpty() {
		BotCharacter character = characterDao.getCharacter();
		return character.getInventory().stream().filter(item -> item.getQuantity() > 0).toList();
	}

	@Override
	public boolean inventoryConstaints(String code, int number) {
		BotCharacter character = characterDao.getCharacter();
		Optional<BotInventoryItem> item = character.getInventory().stream()
				.filter(inventItem -> inventItem.getCode().equals(code)).findFirst();
		return item.isPresent() && item.get().getQuantity() >= number;
	}

	@Override
	public Optional<BotInventoryItem> getFirstEquipementInInventory(List<String> equipementNames) {
		BotCharacter character = characterDao.getCharacter();
		return character.getInventory().stream().filter(inventItem -> equipementNames.contains(inventItem.getCode()))
				.findFirst();
	}

	@Override
	public List<BotInventoryItem> getFilterEquipementInInventory(Collection<String> equipementNames,
			String excludeEquipementName) {
		BotCharacter character = characterDao.getCharacter();
		return character.getInventory().stream()
				.filter(inventItem -> !excludeEquipementName.equals(inventItem.getCode())
						&& equipementNames.contains(inventItem.getCode()))
				.toList();
	}

	private final record SlotQuantityStruct(String slot, int quantity) {
	}
}