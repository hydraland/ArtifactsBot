package strategy.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.response.EquipResponse;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotInventoryItem;
import hydra.model.BotItemReader;
import hydra.model.BotMonster;
import strategy.util.fight.FightService;

public final class MonsterEquipementServiceImpl implements MonsterEquipementService {

	private static final BotCharacterInventorySlot[] SLOTS = new BotCharacterInventorySlot[14];

	static {
		SLOTS[OptimizeResult.UTILITY1_INDEX] = BotCharacterInventorySlot.UTILITY1;
		SLOTS[OptimizeResult.UTILITY2_INDEX] = BotCharacterInventorySlot.UTILITY2;
		SLOTS[OptimizeResult.WEAPON_INDEX] = BotCharacterInventorySlot.WEAPON;
		SLOTS[OptimizeResult.BODY_ARMOR_INDEX] = BotCharacterInventorySlot.BODY_ARMOR;
		SLOTS[OptimizeResult.BOOTS_INDEX] = BotCharacterInventorySlot.BOOTS;
		SLOTS[OptimizeResult.HELMET_INDEX] = BotCharacterInventorySlot.HELMET;
		SLOTS[OptimizeResult.SHIELD_INDEX] = BotCharacterInventorySlot.SHIELD;
		SLOTS[OptimizeResult.LEG_ARMOR_INDEX] = BotCharacterInventorySlot.LEG_ARMOR;
		SLOTS[OptimizeResult.AMULET_INDEX] = BotCharacterInventorySlot.AMULET;
		SLOTS[OptimizeResult.RING1_INDEX] = BotCharacterInventorySlot.RING1;
		SLOTS[OptimizeResult.RING2_INDEX] = BotCharacterInventorySlot.RING2;
		SLOTS[OptimizeResult.ARTIFACT1_INDEX] = BotCharacterInventorySlot.ARTIFACT1;
		SLOTS[OptimizeResult.ARTIFACT2_INDEX] = BotCharacterInventorySlot.ARTIFACT2;
		SLOTS[OptimizeResult.ARTIFACT3_INDEX] = BotCharacterInventorySlot.ARTIFACT3;
	}

	private final FightService fightService;
	private final MoveService moveService;
	private final CharacterDAO characterDao;
	private final CharacterService characterService;
	private final BankDAO bankDao;

	public MonsterEquipementServiceImpl(FightService fightService, CharacterDAO characterDao, BankDAO bankDao,
			CharacterService characterService, MoveService moveService) {
		this.fightService = fightService;
		this.characterDao = characterDao;
		this.characterService = characterService;
		this.moveService = moveService;
		this.bankDao = bankDao;
	}

	@Override
	public boolean equipBestEquipement(BotMonster monster, Map<String, Integer> reservedItems) {
		BotItemInfo[] bestEqt = fightService.optimizeEquipementsPossesed(monster, reservedItems).bestEqt();
		return equipEquipements(bestEqt);
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}

	private boolean equipEquipements(BotItemInfo[] bestEqts) {
		BotCharacter character = characterDao.getCharacter();
		// equipement du perso
		String[] equipedEqt = new String[14];
		equipedEqt[OptimizeResult.UTILITY1_INDEX] = character.getUtility1Slot();
		equipedEqt[OptimizeResult.UTILITY2_INDEX] = character.getUtility2Slot();
		equipedEqt[OptimizeResult.WEAPON_INDEX] = character.getWeaponSlot();
		equipedEqt[OptimizeResult.BODY_ARMOR_INDEX] = character.getBodyArmorSlot();
		equipedEqt[OptimizeResult.BOOTS_INDEX] = character.getBootsSlot();
		equipedEqt[OptimizeResult.HELMET_INDEX] = character.getHelmetSlot();
		equipedEqt[OptimizeResult.SHIELD_INDEX] = character.getShieldSlot();
		equipedEqt[OptimizeResult.LEG_ARMOR_INDEX] = character.getLegArmorSlot();
		equipedEqt[OptimizeResult.AMULET_INDEX] = character.getAmuletSlot();
		equipedEqt[OptimizeResult.RING1_INDEX] = character.getRing1Slot();
		equipedEqt[OptimizeResult.RING2_INDEX] = character.getRing2Slot();
		equipedEqt[OptimizeResult.ARTIFACT1_INDEX] = character.getArtifact1Slot();
		equipedEqt[OptimizeResult.ARTIFACT2_INDEX] = character.getArtifact2Slot();
		equipedEqt[OptimizeResult.ARTIFACT3_INDEX] = character.getArtifact3Slot();

		EquipResponse response = null;
		// Traitement équipement ne posant pas de problème d'unicité
		for (int i : new int[] { OptimizeResult.WEAPON_INDEX, OptimizeResult.BODY_ARMOR_INDEX,
				OptimizeResult.BOOTS_INDEX, OptimizeResult.HELMET_INDEX, OptimizeResult.SHIELD_INDEX,
				OptimizeResult.LEG_ARMOR_INDEX, OptimizeResult.AMULET_INDEX }) {
			if (bestEqts[i] != null) {
				if (!equipedEqt[i].equals(bestEqts[i].botItemDetails().getCode())
						&& !characterService.inventoryConstaints(bestEqts[i].botItemDetails().getCode(), 1)
						&& (!moveService.moveToBank()
								|| !bankDao.withdraw(bestEqts[i].botItemDetails().getCode(), 1))) {
					return false;
				}
				if ("".equals(equipedEqt[i])) {
					response = characterDao.equip(bestEqts[i].botItemDetails(), SLOTS[i], 1);
					if (!response.ok()) {
						return false;
					}
				} else if (!equipedEqt[i].equals(bestEqts[i].botItemDetails().getCode())) {
					response = characterDao.unequip(SLOTS[i], 1);
					if (!response.ok()) {
						return false;
					}
					response = characterDao.equip(bestEqts[i].botItemDetails(), SLOTS[i], 1);
					if (!response.ok()) {
						return false;
					}
				}
			}
		}

		// Traitement des rings
		if (!equipedRingOrArtefact(bestEqts, equipedEqt, OptimizeResult.RING1_INDEX, OptimizeResult.RING2_INDEX)) {
			return false;
		}

		// Traitement des artéfacts
		if (!equipedRingOrArtefact(bestEqts, equipedEqt, OptimizeResult.ARTIFACT1_INDEX, OptimizeResult.ARTIFACT2_INDEX,
				OptimizeResult.ARTIFACT3_INDEX)) {
			return false;
		}

		// Traitement des consommables
		return equipedUtility(bestEqts, equipedEqt);
	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean equipedRingOrArtefact(BotItemInfo[] bestEqts, String[] equipedEqt, int... itemIndex) {
		EquipResponse response;
		List<DiffStruct<Integer>> equipedEqtDiff = new LinkedList<>();
		for (int i : itemIndex) {
			equipedEqtDiff.add(new DiffStruct<>(equipedEqt[i], i));
		}

		List<DiffStruct<Void>> bestEqtDiff = new LinkedList<>();
		for (int i : itemIndex) {
			String code = bestEqts[i] == null ? "" : bestEqts[i].botItemDetails().getCode();
			if (!equipedEqtDiff.remove(new SearchDiffStruct(code))) {
				bestEqtDiff.add(new DiffStruct<>(code, null));
			}
		}

		for (DiffStruct<Integer> equipedStruct : equipedEqtDiff) {
			DiffStruct<Void> bestStruct = bestEqtDiff.removeFirst();
			String bestStructCode = bestStruct.code();
			if (!"".equals(equipedStruct.code())) {
				response = characterDao.unequip(SLOTS[equipedStruct.value()], 1);
				if (!response.ok()) {
					return false;
				}
			}
			if (!characterService.inventoryConstaints(bestStructCode, 1)
					&& (!moveService.moveToBank() || !bankDao.withdraw(bestStructCode, 1))) {
				return false;
			}
			if (!"".equals(bestStructCode)) {
				response = characterDao.equip(bestStructCode, SLOTS[equipedStruct.value()], 1);
				if (!response.ok()) {
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean equipedUtility(BotItemInfo[] bestEqts, String[] equipedEqt) {
		EquipResponse response;
		List<DiffStruct<Integer>> equipedEqtDiff = new LinkedList<>();
		List<DiffStruct<Integer>> equipedEqtSame = new LinkedList<>();
		final int[] utilitiesIndex = new int[] { OptimizeResult.UTILITY1_INDEX, OptimizeResult.UTILITY2_INDEX };
		for (int i : utilitiesIndex) {
			equipedEqtDiff.add(new DiffStruct<>(equipedEqt[i], i));
		}

		List<DiffStruct<Void>> bestEqtDiff = new LinkedList<>();
		for (int i : utilitiesIndex) {
			String code = bestEqts[i] == null ? "" : bestEqts[i].botItemDetails().getCode();
			SearchDiffStruct searchStruct = new SearchDiffStruct(code);
			if (equipedEqtDiff.contains(searchStruct)) {
				equipedEqtSame.add(equipedEqtDiff.remove(equipedEqtDiff.indexOf(searchStruct)));
			} else {
				bestEqtDiff.add(new DiffStruct<>(code, null));
			}
		}

		for (DiffStruct<Integer> equipedStruct : equipedEqtSame) {
			int equipedQuantity = characterService.getUtilitySlotQuantity(SLOTS[equipedStruct.value()]);
			if (equipedQuantity < GameConstants.MAX_ITEM_IN_SLOT && equipedQuantity > 0) {
				Optional<BotInventoryItem> potionInInventory = characterService
						.getFirstEquipementInInventory(Arrays.asList(equipedStruct.code()));
				if (potionInInventory.isPresent()) {
					response = characterDao.equip(equipedStruct.code(), SLOTS[equipedStruct.value()], Math.min(
							GameConstants.MAX_ITEM_IN_SLOT - equipedQuantity, potionInInventory.get().getQuantity()));
					if (!response.ok()) {
						return false;
					}
				}
			}
		}

		for (DiffStruct<Integer> equipedStruct : equipedEqtDiff) {
			DiffStruct<Void> bestStruct = bestEqtDiff.removeFirst();
			boolean unequipOk = false;
			int freeInventorySpace = characterService.getFreeInventorySpace();
			int equipedQuantity = characterService.getUtilitySlotQuantity(SLOTS[equipedStruct.value()]);
			if (equipedQuantity == 0) {
				unequipOk = true;
			} else if (equipedQuantity <= freeInventorySpace) {
				response = characterDao.unequip(SLOTS[equipedStruct.value()], equipedQuantity);
				if (!response.ok() || !moveService.moveToBank()
						|| !bankDao.deposit(equipedStruct.code(), equipedQuantity)) {
					return false;
				}
				freeInventorySpace -= equipedQuantity;
				unequipOk = true;
			} else if (freeInventorySpace > 0) {
				if (!moveService.moveToBank()) {
					return false;
				}
				while (equipedQuantity > 0) {
					int depositQuantity = equipedQuantity > freeInventorySpace ? freeInventorySpace : equipedQuantity;
					response = characterDao.unequip(SLOTS[equipedStruct.value()], depositQuantity);
					if (!response.ok() || !bankDao.deposit(equipedStruct.code(), depositQuantity)) {
						return false;
					}
					equipedQuantity -= depositQuantity;
				}
				unequipOk = true;
			}

			String bestStructCode = bestStruct.code();
			if (unequipOk && !bestStructCode.equals("")) {
				if (!characterService.inventoryConstaints(bestStructCode, 1)) {
					BotItemReader itemInBank = bankDao.getItem(bestStructCode);
					int quantity = Math.min(GameConstants.MAX_ITEM_IN_SLOT,
							Math.min(freeInventorySpace, itemInBank.getQuantity()));
					if (quantity > 0 && (!moveService.moveToBank() || !bankDao.withdraw(bestStructCode, quantity))) {
						return false;
					}
				}
				Optional<BotInventoryItem> potionInInventory = characterService
						.getFirstEquipementInInventory(Arrays.asList(bestStructCode));
				if (potionInInventory.isPresent()) {
					response = characterDao.equip(bestStructCode, SLOTS[equipedStruct.value()],
							Math.min(GameConstants.MAX_ITEM_IN_SLOT, potionInInventory.get().getQuantity()));
					if (!response.ok()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static final record DiffStruct<T>(String code, T value) {
	}

	private static final record SearchDiffStruct(String code) {
		@Override
		public final boolean equals(Object obj) {
			if (obj instanceof DiffStruct<?> ds) {
				return code.equals(ds.code());
			}
			return false;
		}
	}
}
