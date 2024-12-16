package strategy.util.fight;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.dao.response.EquipResponse;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotEffect;
import hydra.model.BotItem;
import hydra.model.BotItemDetails;
import hydra.model.BotMonster;
import strategy.achiever.factory.util.ItemService;
import strategy.util.BotItemInfo;
import strategy.util.CharacterService;
import strategy.util.ItemOrigin;
import strategy.util.MoveService;
import strategy.util.OptimizeResult;
import util.CacheManager;
import util.Combinator;
import util.LimitedTimeCacheManager;

public final class FightServiceImpl implements FightService {
	private final CharacterDAO characterDao;
	private final CharacterService characterService;
	private static final BotCharacterInventorySlot[] SLOTS = new BotCharacterInventorySlot[] {
			BotCharacterInventorySlot.WEAPON, BotCharacterInventorySlot.BODY_ARMOR, BotCharacterInventorySlot.BOOTS,
			BotCharacterInventorySlot.HELMET, BotCharacterInventorySlot.SHIELD, BotCharacterInventorySlot.LEG_ARMOR,
			BotCharacterInventorySlot.AMULET, BotCharacterInventorySlot.RING1, BotCharacterInventorySlot.RING2,
			BotCharacterInventorySlot.UTILITY1, BotCharacterInventorySlot.UTILITY2, BotCharacterInventorySlot.ARTIFACT1,
			BotCharacterInventorySlot.ARTIFACT2, BotCharacterInventorySlot.ARTIFACT3 };
	private final BankDAO bankDao;
	private final MoveService moveService;
	private final ItemDAO itemDAO;
	private final CacheManager<String, OptimizeResult> optimizeCacheManager;
	private final ItemService itemService;
	private final HashMap<String, UtilityStruct> useUtilityMap;

	public FightServiceImpl(CharacterDAO characterDao, BankDAO bankDao, ItemDAO itemDAO,
			CharacterService characterService, MoveService moveService, ItemService itemService) {
		this.characterDao = characterDao;
		this.bankDao = bankDao;
		this.itemDAO = itemDAO;
		this.characterService = characterService;
		this.moveService = moveService;
		this.itemService = itemService;
		this.optimizeCacheManager = new LimitedTimeCacheManager<>(3600);
		this.useUtilityMap = new HashMap<>();
	}

	@Override
	public OptimizeResult optimizeEquipementsInInventory(BotMonster monster, Map<String, Integer> reservedItems) {
		boolean useUtility = useUtilityMap.get(monster.getCode()) == null
				|| useUtilityMap.get(monster.getCode()).utilityUsed();
		Map<BotCharacterInventorySlot, List<BotItemInfo>> equipableCharacterEquipement = characterService
				.getEquipableCharacterEquipement(reservedItems, useUtility);
		return optimizeEquipements(monster, equipableCharacterEquipement, useUtility);
	}

	@Override
	public OptimizeResult optimizeEquipementsPossesed(BotMonster monster, Map<String, Integer> reservedItems) {
		boolean useUtility = useUtilityMap.get(monster.getCode()) == null
				|| useUtilityMap.get(monster.getCode()).utilityUsed();
		Map<BotCharacterInventorySlot, List<BotItemInfo>> equipableCharacterEquipement = getAllCharacterEquipments(
				reservedItems, useUtility);
		return optimizeEquipements(monster, equipableCharacterEquipement, useUtility);
	}

	@Override
	public Map<String, OptimizeResult> optimizeEquipementsPossesed(List<BotMonster> monsters,
			Map<String, Integer> reservedItems) {
		Map<BotCharacterInventorySlot, List<BotItemInfo>> equipableCharacterEquipement = getAllCharacterEquipments(
				reservedItems, true);
		Map<String, OptimizeResult> result = new HashMap<>();
		for (BotMonster monster : monsters) {
			result.computeIfAbsent(monster.getCode(),
					c -> optimizeEquipements(monster, equipableCharacterEquipement, true));
		}
		return result;
	}

	private Map<BotCharacterInventorySlot, List<BotItemInfo>> getAllCharacterEquipments(
			Map<String, Integer> reservedItems, boolean useUtility) {
		Map<String, Integer> ignoreItems = new HashMap<>(reservedItems);
		// On ignore les tools, ne sont pas fait pour le combat
		addIgnoreItems(ignoreItems, itemService.getToolsCode());
		Map<BotCharacterInventorySlot, List<BotItemInfo>> equipableCharacterEquipement = characterService
				.getEquipableCharacterEquipement(ignoreItems, useUtility);
		// On ignore les équipements que l'on a dans l'inventaire ou sur le perso avec
		// la particularité des ring
		addIgnoreItems(ignoreItems, equipableCharacterEquipement);
		Map<BotCharacterInventorySlot, List<BotItemInfo>> equipableCharacterEquipementInBank = characterService
				.getEquipableCharacterEquipementInBank(bankDao, ignoreItems, useUtility);

		for (Entry<BotCharacterInventorySlot, List<BotItemInfo>> entry : equipableCharacterEquipement.entrySet()) {
			List<BotItemInfo> list = equipableCharacterEquipementInBank.get(entry.getKey());
			if (list != null) {
				entry.getValue().addAll(list);
			}
		}
		return equipableCharacterEquipement;
	}

	private void addIgnoreItems(Map<String, Integer> ignoreItems,
			Map<BotCharacterInventorySlot, List<BotItemInfo>> equipableCharacterEquipement) {
		for (Entry<BotCharacterInventorySlot, List<BotItemInfo>> entry : equipableCharacterEquipement.entrySet()) {
			if (!BotCharacterInventorySlot.RING1.equals(entry.getKey())
					&& !BotCharacterInventorySlot.RING2.equals(entry.getKey())) {
				entry.getValue().forEach(bii -> ignoreItems.put(bii.botItemDetails().getCode(), 0));
			} else {
				// On ignore que s'il y a au moins 2 ring
				entry.getValue().stream().filter(bii -> bii.quantity() > 1)
						.forEach(bii -> ignoreItems.put(bii.botItemDetails().getCode(), 0));
			}
		}
	}

	private void addIgnoreItems(Map<String, Integer> ignoreItems, List<String> toolsCode) {
		toolsCode.stream().forEach(tc -> ignoreItems.put(tc, 0));
	}

	private String createKey(String code,
			Map<BotCharacterInventorySlot, List<BotItemInfo>> equipableCharacterEquipement) {
		StringBuilder builder = new StringBuilder(code);
		equipableCharacterEquipement.entrySet().stream()
				.forEach(entry -> builder.append(entry.getKey()).append(Objects.hash(entry.getValue().toArray())));
		return builder.toString();
	}

	@Override
	public OptimizeResult optimizeEquipements(BotMonster monster,
			Map<BotCharacterInventorySlot, List<BotItemInfo>> equipableCharacterEquipement, boolean useUtility) {
		String key = createKey(monster.getCode(), equipableCharacterEquipement);
		if (optimizeCacheManager.contains(key)) {
			return optimizeCacheManager.get(key);
		}
		List<BotItemInfo> weaponCharacter = equipableCharacterEquipement.get(BotCharacterInventorySlot.WEAPON);
		List<BotItemInfo> bodyArmorCharacter = equipableCharacterEquipement.get(BotCharacterInventorySlot.BODY_ARMOR);
		List<BotItemInfo> bootsCharacter = equipableCharacterEquipement.get(BotCharacterInventorySlot.BOOTS);
		List<BotItemInfo> helmetCharacter = equipableCharacterEquipement.get(BotCharacterInventorySlot.HELMET);
		List<BotItemInfo> shieldCharacter = equipableCharacterEquipement.get(BotCharacterInventorySlot.SHIELD);
		List<BotItemInfo> legArmorCharacter = equipableCharacterEquipement.get(BotCharacterInventorySlot.LEG_ARMOR);
		List<BotItemInfo> amulerCharacter = equipableCharacterEquipement.get(BotCharacterInventorySlot.AMULET);
		List<BotItemInfo> ring1Character = equipableCharacterEquipement.get(BotCharacterInventorySlot.RING1);
		List<BotItemInfo> ring2Character = equipableCharacterEquipement.get(BotCharacterInventorySlot.RING2);
		List<BotItemInfo> utility1Character = equipableCharacterEquipement.get(BotCharacterInventorySlot.UTILITY1);
		List<BotItemInfo> utility2Character = equipableCharacterEquipement.get(BotCharacterInventorySlot.UTILITY2);
		List<BotItemInfo> artifact1Character = equipableCharacterEquipement.get(BotCharacterInventorySlot.ARTIFACT1);
		List<BotItemInfo> artifact2Character = equipableCharacterEquipement.get(BotCharacterInventorySlot.ARTIFACT2);
		List<BotItemInfo> artifact3Character = equipableCharacterEquipement.get(BotCharacterInventorySlot.ARTIFACT3);

		// On ajoute null au cas ou un item serait mauvais et pour les items multiples
		addNullValueIfAbsent(weaponCharacter);
		addNullValueIfAbsent(bodyArmorCharacter);
		addNullValueIfAbsent(bootsCharacter);
		addNullValueIfAbsent(helmetCharacter);
		addNullValueIfAbsent(shieldCharacter);
		addNullValueIfAbsent(legArmorCharacter);
		addNullValueIfAbsent(amulerCharacter);
		addNullValueIfAbsent(ring1Character);
		addNullValueIfAbsent(ring2Character);
		addNullValueIfAbsent(utility1Character);
		addNullValueIfAbsent(utility2Character);
		addNullValueIfAbsent(artifact1Character);
		addNullValueIfAbsent(artifact2Character);
		addNullValueIfAbsent(artifact3Character);

		Combinator<BotItemInfo> combinator = new Combinator<>(BotItemInfo.class, 14);
		combinator.set(0, weaponCharacter);
		combinator.set(1, bodyArmorCharacter);
		combinator.set(2, bootsCharacter);
		combinator.set(3, helmetCharacter);
		combinator.set(4, shieldCharacter);
		combinator.set(5, legArmorCharacter);
		combinator.set(6, amulerCharacter);
		combinator.set(7, ring1Character);
		combinator.set(8, ring2Character);
		combinator.set(9, utility1Character);
		combinator.set(10, utility2Character);
		combinator.set(11, artifact1Character);
		combinator.set(12, artifact2Character);
		combinator.set(13, artifact3Character);

		BotItemInfo[] bestEquipements = initBestEquipments(characterDao.getCharacter(), useUtility);
		int characterHp = characterService.getCharacterHPWihtoutEquipment();
		FightDetails maxFightDetails = initOptimizeResultWithEquipedItems(characterDao.getCharacter(), monster,
				characterHp, useUtility);

		Map<BotEffect, Float> effectMap = resetEffectMap();
		for (BotItemInfo[] botItemInfos : combinator) {
			if (validCombinaison(botItemInfos)) {
				for (BotItemInfo botItemInfo : botItemInfos) {
					if (botItemInfo != null) {
						updateEffectInMap(effectMap, botItemInfo.botItemDetails(), botItemInfo.quantity());
					}
				}

				// Evaluation
				FightDetails currentFightDetails = optimizeVictory(effectMap, monster, characterHp);
				if (currentFightDetails.eval() > maxFightDetails.eval()) {
					maxFightDetails = currentFightDetails;
					bestEquipements = botItemInfos.clone();
				}
				effectMap = resetEffectMap();
			}
		}

		if (bestEquipements == null) {
			// Possible que si le perso à 0 équipement.
			bestEquipements = new BotItemInfo[14];

			// Evaluation
			maxFightDetails = new FightDetails(0, 1, 0, 0);
		}

		OptimizeResult result = new OptimizeResult(maxFightDetails, bestEquipements);
		optimizeCacheManager.add(key, result);
		useUtilityMap.put(monster.getCode(),
				new UtilityStruct(maxFightDetails.eval(), bestEquipements[9] != null || bestEquipements[10] != null));
		return result;
	}

	private void addNullValueIfAbsent(List<BotItemInfo> botItemList) {
		if (!botItemList.contains(null)) {
			botItemList.add(null);
		}
	}

	@Override
	public FightDetails calculateFightResult(BotMonster monster) {
		int characterHp = characterService.getCharacterHPWihtoutEquipment();
		return initOptimizeResultWithEquipedItems(characterDao.getCharacter(), monster, characterHp, true);
	}

	private BotItemInfo[] initBestEquipments(BotCharacter character, boolean useUtility) {
		return new BotItemInfo[] { initBestEquipement(character.getWeaponSlot(), 1),
				initBestEquipement(character.getBodyArmorSlot(), 1), initBestEquipement(character.getBootsSlot(), 1),
				initBestEquipement(character.getHelmetSlot(), 1), initBestEquipement(character.getShieldSlot(), 1),
				initBestEquipement(character.getLegArmorSlot(), 1), initBestEquipement(character.getAmuletSlot(), 1),
				initBestEquipement(character.getRing1Slot(), 1), initBestEquipement(character.getRing2Slot(), 1),
				useUtility ? initBestEquipement(character.getUtility1Slot(), character.getUtility1SlotQuantity())
						: null,
				useUtility ? initBestEquipement(character.getUtility2Slot(), character.getUtility2SlotQuantity())
						: null,
				initBestEquipement(character.getArtifact1Slot(), 1),
				initBestEquipement(character.getArtifact2Slot(), 1),
				initBestEquipement(character.getArtifact3Slot(), 1) };
	}

	private BotItemInfo initBestEquipement(String slot, int quantity) {
		return "".equals(slot) ? null : new BotItemInfo(itemDAO.getItem(slot), quantity, ItemOrigin.ON_SELF);
	}

	private FightDetails initOptimizeResultWithEquipedItems(BotCharacter character, BotMonster monster, int characterHp,
			boolean useUtility) {
		Map<BotEffect, Float> effectMap = resetEffectMap();

		updateEffectInMapForEquipedEqt(character.getWeaponSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getBodyArmorSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getBootsSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getHelmetSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getShieldSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getLegArmorSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getAmuletSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getRing1Slot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getRing2Slot(), effectMap, 1);
		if (useUtility) {
			updateEffectInMapForEquipedEqt(character.getUtility1Slot(), effectMap, character.getUtility1SlotQuantity());
			updateEffectInMapForEquipedEqt(character.getUtility2Slot(), effectMap, character.getUtility2SlotQuantity());
		}
		updateEffectInMapForEquipedEqt(character.getArtifact1Slot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getArtifact2Slot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getArtifact3Slot(), effectMap, 1);

		return optimizeVictory(effectMap, monster, characterHp);
	}

	private void updateEffectInMapForEquipedEqt(String slot, Map<BotEffect, Float> effectMap, int quantity) {
		if (!"".equals(slot)) {
			updateEffectInMap(effectMap, itemDAO.getItem(slot), quantity);
		}
	}

	private boolean validCombinaison(BotItemInfo[] botItemInfos) {
		Set<String> uniqueEquipItem = new HashSet<>();
		for (int i = 9; i < botItemInfos.length; i++) {
			if (botItemInfos[i] != null) {
				if (uniqueEquipItem.contains(botItemInfos[i].botItemDetails().getCode())) {
					return false;
				} else {
					uniqueEquipItem.add(botItemInfos[i].botItemDetails().getCode());
				}
			}
		}

		// cas ou les rings sont dans l'inventaire, à la bank ou les 2
		if (botItemInfos[7] != null && botItemInfos[8] != null
				&& botItemInfos[7].botItemDetails().getCode().equals(botItemInfos[8].botItemDetails().getCode())) {
			return botItemInfos[7].quantity() > 1 || botItemInfos[8].quantity() > 1
					|| !botItemInfos[7].origin().equals(botItemInfos[8].origin());
		}
		return true;
	}

	// TODO voir gestion restore correctement (et non a minima) et les blocks
	private FightDetails optimizeVictory(Map<BotEffect, Float> effectMap, BotMonster monster, int characterHp) {
		double characterTurn = monster.getHp() / calculCharacterDamage(effectMap, monster);
		double monsterTurn = (characterHp + effectMap.get(BotEffect.HP) + effectMap.get(BotEffect.BOOST_HP)
				+ effectMap.get(BotEffect.RESTORE)) / calculMonsterDamage(effectMap, monster);
		long nbTurn = Math.round(Math.min(characterTurn, monsterTurn));
		return new FightDetails(monsterTurn / characterTurn, nbTurn,
				(long) Math.min(characterHp + effectMap.get(BotEffect.HP),
						(characterHp + effectMap.get(BotEffect.HP) + effectMap.get(BotEffect.BOOST_HP))
								- Math.round(nbTurn * calculMonsterDamage(effectMap, monster))),
				monster.getHp() - Math.round(nbTurn * calculCharacterDamage(effectMap, monster)));
	}

	private void updateEffectInMap(Map<BotEffect, Float> effectMap, BotItemDetails botItemDetail, int quantity) {
		// On donne un petit boost au restore
		botItemDetail.getEffects().stream()
				.forEach(effect -> effectMap.put(effect.getName(), effect.getValue() + effectMap.get(effect.getName())
						+ (BotEffect.RESTORE.equals(effect.getName()) ? quantity / 100f : 0)));
	}

	private Map<BotEffect, Float> resetEffectMap() {
		Map<BotEffect, Float> effectMap = new EnumMap<>(BotEffect.class);
		for (BotEffect botEffect : BotEffect.values()) {
			effectMap.put(botEffect, 0f);
		}
		return effectMap;
	}

	private double calculMonsterDamage(Map<BotEffect, Float> effectMap, BotMonster monster) {
		double monsterEartDmg = monster.getAttackEarth()
				* (1d - (effectMap.get(BotEffect.RES_EARTH) + effectMap.get(BotEffect.BOOST_RES_EARTH)) / 100d);
		double monsterAirDmg = monster.getAttackAir()
				* (1d - (effectMap.get(BotEffect.RES_AIR) + effectMap.get(BotEffect.BOOST_RES_AIR)) / 100d);
		double monsterWaterDmg = monster.getAttackWater()
				* (1d - (effectMap.get(BotEffect.RES_WATER) + effectMap.get(BotEffect.BOOST_RES_WATER)) / 100d);
		double monsterFireDmg = monster.getAttackFire()
				* (1d - (effectMap.get(BotEffect.RES_FIRE) + effectMap.get(BotEffect.BOOST_RES_FIRE)) / 100d);
		return monsterEartDmg + monsterAirDmg + monsterWaterDmg + monsterFireDmg;
	}

	private double calculCharacterDamage(Map<BotEffect, Float> effectMap, BotMonster monster) {
		int characterEartDmg = calculEffectDamage(effectMap.get(BotEffect.ATTACK_EARTH),
				effectMap.get(BotEffect.DMG_EARTH), effectMap.get(BotEffect.BOOST_DMG_EARTH), monster.getResEarth());
		int characterAirDmg = calculEffectDamage(effectMap.get(BotEffect.ATTACK_AIR), effectMap.get(BotEffect.DMG_AIR),
				effectMap.get(BotEffect.BOOST_DMG_AIR), monster.getResAir());
		int characterWaterDmg = calculEffectDamage(effectMap.get(BotEffect.ATTACK_WATER),
				effectMap.get(BotEffect.DMG_WATER), effectMap.get(BotEffect.BOOST_DMG_WATER), monster.getResWater());
		int characterFireDmg = calculEffectDamage(effectMap.get(BotEffect.ATTACK_FIRE),
				effectMap.get(BotEffect.DMG_FIRE), effectMap.get(BotEffect.BOOST_DMG_FIRE), monster.getResFire());
		return characterEartDmg + characterAirDmg + characterWaterDmg + characterFireDmg;
	}

	private int calculEffectDamage(float attackDmg, float dmgPercent, float dmgBoost, float monsterRes) {
		return (int) (attackDmg * ((100d + dmgPercent + dmgBoost) * (100d - monsterRes)) / 10000);
	}

	@Override
	public boolean equipEquipements(BotItemInfo[] bestEqts) {
		BotCharacter character = characterDao.getCharacter();
		// equipement du perso
		String[] equipedEqt = new String[] { character.getWeaponSlot(), character.getBodyArmorSlot(),
				character.getBootsSlot(), character.getHelmetSlot(), character.getShieldSlot(),
				character.getLegArmorSlot(), character.getAmuletSlot(), character.getRing1Slot(),
				character.getRing2Slot(), character.getUtility1Slot(), character.getUtility2Slot(),
				character.getArtifact1Slot(), character.getArtifact2Slot(), character.getArtifact3Slot() };

		if (containtsItemBankOrigin(bestEqts) && !moveService.moveToBank()) {
			return false;
		}

		EquipResponse response = null;
		// Traitement équipement ne posant pas de problème d'unicité
		for (int i = 0; i < 7; i++) {
			if (bestEqts[i] != null) {
				if (bestEqts[i].origin().equals(ItemOrigin.BANK)) {
					BotItem botItem = new BotItem();
					botItem.setCode(bestEqts[i].botItemDetails().getCode());
					botItem.setQuantity(1);
					if (!bankDao.withdraw(botItem)) {
						return false;
					}
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
		return equipedConsomable(bestEqts, equipedEqt);
	}

	private boolean containtsItemBankOrigin(BotItemInfo[] bestEqts) {
		for (int i = 0; i < bestEqts.length; i++) {
			BotItemInfo bestEqt = bestEqts[i];
			if (bestEqt != null && bestEqt.origin().equals(ItemOrigin.BANK)) {
				return true;
			}
		}
		return false;
	}

	private boolean equipedConsomable(BotItemInfo[] bestEqts, String[] equipedEqt) {
		EquipResponse response;
		List<String> bestConsomableToEquip = new ArrayList<>();
		for (int i = 9; i <= 10; i++) {
			if (bestEqts[i] != null) {
				bestConsomableToEquip.add(bestEqts[i].botItemDetails().getCode());
			}
		}
		if (!bestConsomableToEquip.isEmpty()) {
			List<String> equipedConsomable = new ArrayList<>();
			for (int i = 9; i <= 10; i++) {
				if (!"".equals(equipedEqt[i])) {
					equipedConsomable.add(equipedEqt[i]);
				}
			}

			int firstInsertPlace = 9;
			for (int i = 9; i <= 10; i++) {
				if (bestEqts[i] != null) {
					String bestEqtCode = bestEqts[i].botItemDetails().getCode();
					if (equipedConsomable.contains(bestEqtCode)) {
						// search equiped slot
						int indexEquiped = bestEqtCode.equals(equipedEqt[9]) ? 9 : 10;
						BotItemInfo itemInfo = new BotItemInfo(bestEqts[i].botItemDetails(),
								bestEqts[i].quantity() - characterService.getUtilitySlotQuantity(SLOTS[indexEquiped]),
								bestEqts[i].origin());
						int quantityToEquip = characterService.getQuantityEquipableForUtility(itemInfo,
								SLOTS[indexEquiped]);
						if (quantityToEquip > 0) {
							response = characterDao.equip(bestEqts[i].botItemDetails(), SLOTS[indexEquiped],
									quantityToEquip);
							if (!response.ok()) {
								return false;
							}
						}
					} else {
						// search insert place
						boolean findInsertPlace = false;
						for (int j = firstInsertPlace; j <= 10; j++) {
							int consumableSlotQuantity = characterService
									.getUtilitySlotQuantity(SLOTS[firstInsertPlace]);
							if (!bestConsomableToEquip.contains(equipedEqt[j]) && ("".equals(equipedEqt[j])
									|| consumableSlotQuantity < characterService.getFreeInventorySpace())) {
								firstInsertPlace = j;
								findInsertPlace = true;
								break;
							}
						}
						if (!findInsertPlace) {
							// Pas de place libre trouvé tant pis on considère que c'est OK
							return true;
						}
						int quantityToEquip = characterService.getQuantityEquipableForUtility(bestEqts[i],
								SLOTS[firstInsertPlace]);
						if (bestEqts[i].origin().equals(ItemOrigin.BANK)) {
							BotItem botItem = new BotItem();
							botItem.setCode(bestEqts[i].botItemDetails().getCode());
							botItem.setQuantity(Math.min(quantityToEquip, characterService.getFreeInventorySpace()));
							if (botItem.getQuantity() > 0 && !bankDao.withdraw(botItem)) {
								return false;
							}
						}
						if ("".equals(equipedEqt[firstInsertPlace])) {
							response = characterDao.equip(bestEqts[i].botItemDetails(), SLOTS[firstInsertPlace],
									quantityToEquip);
							if (!response.ok()) {
								return false;
							}
						} else {
							int consumableSlotQuantity = characterService
									.getUtilitySlotQuantity(SLOTS[firstInsertPlace]);
							if (consumableSlotQuantity > characterService.getFreeInventorySpace()) {
								// Pas assez d'espace libre
								return true;
							}
							response = characterDao.unequip(SLOTS[firstInsertPlace], consumableSlotQuantity);
							if (!response.ok()) {
								return false;
							}
							response = characterDao.equip(bestEqts[i].botItemDetails(), SLOTS[firstInsertPlace],
									quantityToEquip);
							if (!response.ok()) {
								return false;
							}
						}
						equipedEqt[firstInsertPlace] = bestEqtCode; // Maj des équipements équipés
					}
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean equipedRingOrArtefact(BotItemInfo[] bestEqts, String[] equipedEqt, int minRange,
			int maxExcludeRange) {
		EquipResponse response;
		List<DiffStruct<Integer>> equipedEqtDiff = new ArrayList<>();
		for (int i = minRange; i < maxExcludeRange; i++) {
			equipedEqtDiff.add(new DiffStruct<Integer>(equipedEqt[i], i));
		}

		List<DiffStruct<ItemOrigin>> bestEqtDiff = new ArrayList<>();
		for (int i = minRange; i < maxExcludeRange; i++) {
			ItemOrigin origin = bestEqts[i] == null ? ItemOrigin.ON_SELF : bestEqts[i].origin();
			String code = bestEqts[i] == null ? "" : bestEqts[i].botItemDetails().getCode();
			if (ItemOrigin.BANK.equals(origin) || !equipedEqtDiff.remove(new SearchDiffStruct(code))) {
				bestEqtDiff.add(new DiffStruct<ItemOrigin>(code, origin));
			}
		}

		for (DiffStruct<Integer> equipedStruct : equipedEqtDiff) {
			DiffStruct<ItemOrigin> bestStruct = bestEqtDiff.removeFirst();
			if (bestStruct.value().equals(ItemOrigin.BANK)) {
				BotItem botItem = new BotItem();
				botItem.setCode(bestStruct.code());
				botItem.setQuantity(1);
				if (!bankDao.withdraw(botItem)) {
					return false;
				}
			}

			if (!"".equals(equipedStruct.code())) {
				response = characterDao.unequip(SLOTS[equipedStruct.value()], 1);
				if (!response.ok()) {
					return false;
				}
			}
			response = characterDao.equip(bestStruct.code(), SLOTS[equipedStruct.value()], 1);
			if (!response.ok()) {
				return false;
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
