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

	private static final BotCharacterInventorySlot[] SLOTS = new BotCharacterInventorySlot[] {
			BotCharacterInventorySlot.WEAPON, BotCharacterInventorySlot.BODY_ARMOR, BotCharacterInventorySlot.BOOTS,
			BotCharacterInventorySlot.HELMET, BotCharacterInventorySlot.SHIELD, BotCharacterInventorySlot.LEG_ARMOR,
			BotCharacterInventorySlot.AMULET, BotCharacterInventorySlot.RING1, BotCharacterInventorySlot.RING2,
			BotCharacterInventorySlot.UTILITY1, BotCharacterInventorySlot.UTILITY2, BotCharacterInventorySlot.ARTIFACT1,
			BotCharacterInventorySlot.ARTIFACT2, BotCharacterInventorySlot.ARTIFACT3 };

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
		String[] equipedEqt = new String[] { character.getWeaponSlot(), character.getBodyArmorSlot(),
				character.getBootsSlot(), character.getHelmetSlot(), character.getShieldSlot(),
				character.getLegArmorSlot(), character.getAmuletSlot(), character.getRing1Slot(),
				character.getRing2Slot(), character.getUtility1Slot(), character.getUtility2Slot(),
				character.getArtifact1Slot(), character.getArtifact2Slot(), character.getArtifact3Slot() };

		EquipResponse response = null;
		// Traitement équipement ne posant pas de problème d'unicité
		for (int i = 0; i < 7; i++) {
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
		if (!equipedRingOrArtefact(bestEqts, equipedEqt, 7, 9)) {
			return false;
		}

		// Traitement des artéfacts
		if (!equipedRingOrArtefact(bestEqts, equipedEqt, 11, bestEqts.length)) {
			return false;
		}

		// Traitement des consommables
		return equipedUtility(bestEqts, equipedEqt);
	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean equipedRingOrArtefact(BotItemInfo[] bestEqts, String[] equipedEqt, int minRange,
			int maxExcludeRange) {
		EquipResponse response;
		List<DiffStruct<Integer>> equipedEqtDiff = new LinkedList<>();
		for (int i = minRange; i < maxExcludeRange; i++) {
			equipedEqtDiff.add(new DiffStruct<>(equipedEqt[i], i));
		}

		List<DiffStruct<Void>> bestEqtDiff = new LinkedList<>();
		for (int i = minRange; i < maxExcludeRange; i++) {
			String code = bestEqts[i] == null ? "" : bestEqts[i].botItemDetails().getCode();
			if (!equipedEqtDiff.remove(new SearchDiffStruct(code))) {
				bestEqtDiff.add(new DiffStruct<>(code, null));
			}
		}

		for (DiffStruct<Integer> equipedStruct : equipedEqtDiff) {
			DiffStruct<Void> bestStruct = bestEqtDiff.removeFirst();
			String bestStructCode = bestStruct.code();
			if (!"".equals(bestStructCode)) {
				response = characterDao.unequip(SLOTS[equipedStruct.value()], 1);
				if (!response.ok()) {
					return false;
				}
			}
			if (!characterService.inventoryConstaints(bestStructCode, 1)
					&& (!moveService.moveToBank() || !bankDao.withdraw(bestStructCode, 1))) {
				return false;
			}
			response = characterDao.equip(bestStructCode, SLOTS[equipedStruct.value()], 1);
			if (!response.ok()) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean equipedUtility(BotItemInfo[] bestEqts, String[] equipedEqt) {
		EquipResponse response;
		List<DiffStruct<Integer>> equipedEqtDiff = new LinkedList<>();
		List<DiffStruct<Integer>> equipedEqtSame = new LinkedList<>();
		for (int i = 9; i <= 10; i++) {
			equipedEqtDiff.add(new DiffStruct<>(equipedEqt[i], i));
		}

		List<DiffStruct<Void>> bestEqtDiff = new LinkedList<>();
		for (int i = 9; i <= 10; i++) {
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
