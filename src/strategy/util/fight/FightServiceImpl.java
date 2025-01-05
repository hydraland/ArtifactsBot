package strategy.util.fight;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotEffect;
import hydra.model.BotItemDetails;
import hydra.model.BotItemType;
import hydra.model.BotMonster;
import strategy.achiever.factory.util.ItemService;
import strategy.util.BotItemInfo;
import strategy.util.CharacterService;
import strategy.util.OptimizeResult;
import util.CacheManager;
import util.Combinator;
import util.LimitedTimeCacheManager;

public final class FightServiceImpl implements FightService {
	private static final FightDetails DEFAULT_FIGHT_DETAILS = new FightDetails(false, GameConstants.MAX_FIGHT_TURN,
			GameConstants.MAX_FIGHT_TURN, Integer.MAX_VALUE, 0);
	private final CharacterDAO characterDao;
	private final CharacterService characterService;
	private final BankDAO bankDao;
	private final ItemDAO itemDAO;
	private final CacheManager<String, OptimizeResult> optimizeCacheManager;
	private final ItemService itemService;
	private final Set<String> noUseUtilityMonsterCode;
	private final Set<String> oddItems;

	public FightServiceImpl(CharacterDAO characterDao, BankDAO bankDao, ItemDAO itemDAO,
			CharacterService characterService, ItemService itemService) {
		this.characterDao = characterDao;
		this.bankDao = bankDao;
		this.itemDAO = itemDAO;
		this.characterService = characterService;
		this.itemService = itemService;
		// 1 semaine 3600*168
		this.optimizeCacheManager = new LimitedTimeCacheManager<>(3600 * 168);
		this.noUseUtilityMonsterCode = new HashSet<>();
		this.oddItems = new HashSet<>();
	}

	@Override
	public OptimizeResult optimizeEquipementsInInventory(BotMonster monster, Map<String, Integer> reservedItems) {
		boolean useUtility = !noUseUtilityMonsterCode.contains(monster.getCode());
		Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement = characterService
				.getEquipableCharacterEquipement(reservedItems, useUtility);
		return optimizeEquipements(monster, equipableCharacterEquipement);
	}

	@Override
	public OptimizeResult optimizeEquipementsPossesed(BotMonster monster, Map<String, Integer> reservedItems) {
		boolean useUtility = !noUseUtilityMonsterCode.contains(monster.getCode());
		Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement = getAllCharacterEquipments(reservedItems,
				useUtility);
		return optimizeEquipements(monster, equipableCharacterEquipement);
	}

	@Override
	public Map<String, OptimizeResult> optimizeEquipementsPossesed(List<BotMonster> monsters,
			Map<String, Integer> reservedItems) {
		Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement = getAllCharacterEquipments(reservedItems,
				true);
		Map<String, OptimizeResult> result = new HashMap<>();
		for (BotMonster monster : monsters) {
			result.computeIfAbsent(monster.getCode(), c -> optimizeEquipements(monster, equipableCharacterEquipement));
		}
		return result;
	}

	private Map<BotItemType, List<BotItemInfo>> getAllCharacterEquipments(Map<String, Integer> reservedItems,
			boolean useUtility) {
		Map<String, Integer> ignoreItems = new HashMap<>(reservedItems);
		// On ignore les items d�pass�
		addIgnoreItems(ignoreItems, oddItems);
		// On ignore les tools, ne sont pas fait pour le combat
		addIgnoreItems(ignoreItems, itemService.getToolsCode());
		Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement = characterService
				.getEquipableCharacterEquipement(ignoreItems, useUtility);
		// On ignore les �quipements que l'on a dans l'inventaire ou sur le perso avec
		// la particularit� des ring
		addIgnoreItems(ignoreItems, equipableCharacterEquipement);
		Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipementInBank = characterService
				.getEquipableCharacterEquipementInBank(bankDao, ignoreItems, useUtility);

		for (Entry<BotItemType, List<BotItemInfo>> entry : equipableCharacterEquipement.entrySet()) {
			List<BotItemInfo> list = equipableCharacterEquipementInBank.get(entry.getKey());
			if (list != null) {
				if (BotItemType.RING.equals(entry.getKey())) {
					List<BotItemInfo> ringsPreserved = new LinkedList<>();
					Map<String, BotItemInfo> ringsSingle = new HashMap<>();
					entry.getValue().forEach(bii -> {
						if (bii.quantity() == 1) {
							ringsSingle.put(bii.botItemDetails().getCode(), bii);
						} else {
							ringsPreserved.add(bii);
						}
					});
					list.forEach(bii -> {
						if (ringsSingle.containsKey(bii.botItemDetails().getCode())) {
							if (bii.quantity() > 1) {
								ringsPreserved.add(bii);
							} else {
								ringsPreserved.add(new BotItemInfo(bii.botItemDetails(), 2));
							}
							ringsSingle.remove(bii.botItemDetails().getCode());
						} else {
							ringsPreserved.add(bii);
						}
					});
					entry.getValue().clear();
					entry.getValue().addAll(ringsPreserved);
					entry.getValue().addAll(ringsSingle.values());
				} else {
					entry.getValue().addAll(list);
				}
			}
		}
		return equipableCharacterEquipement;
	}

	private void addIgnoreItems(Map<String, Integer> ignoreItems,
			Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement) {
		for (Entry<BotItemType, List<BotItemInfo>> entry : equipableCharacterEquipement.entrySet()) {
			if (!BotItemType.RING.equals(entry.getKey())) {
				entry.getValue().forEach(bii -> ignoreItems.put(bii.botItemDetails().getCode(), 0));
			} else {
				// On ignore que s'il y a au moins 2 ring
				entry.getValue().stream().filter(bii -> bii.quantity() > 1)
						.forEach(bii -> ignoreItems.put(bii.botItemDetails().getCode(), 0));
			}
		}
	}

	private void addIgnoreItems(Map<String, Integer> ignoreItems, Collection<String> itemsCode) {
		itemsCode.stream().forEach(tc -> ignoreItems.put(tc, 0));
	}

	private OptimizeResult optimizeEquipements(BotMonster monster,
			Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement) {
		int characterHp = characterService.getCharacterHPWithoutEquipment();
		return optimizeEquipements(monster, equipableCharacterEquipement, characterHp);
	}

	@Override
	public OptimizeResult optimizeEquipements(BotMonster monster,
			Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement, int characterHpWithoutEqt) {
		String key = FightServiceUtils.createKey(characterHpWithoutEqt, monster.getCode(),
				equipableCharacterEquipement);
		if (optimizeCacheManager.contains(key)) {
			return optimizeCacheManager.get(key);
		}
		List<BotItemInfo> weaponCharacter = sortItemsByLevel(
				filterOdd(equipableCharacterEquipement.get(BotItemType.WEAPON)));
		List<BotItemInfo> bodyArmorCharacter = sortItemsByLevel(
				filterOdd(equipableCharacterEquipement.get(BotItemType.BODY_ARMOR)));
		List<BotItemInfo> bootsCharacter = sortItemsByLevel(
				filterOdd(equipableCharacterEquipement.get(BotItemType.BOOTS)));
		List<BotItemInfo> helmetCharacter = sortItemsByLevel(
				filterOdd(equipableCharacterEquipement.get(BotItemType.HELMET)));
		List<BotItemInfo> shieldCharacter = sortItemsByLevel(
				filterOdd(equipableCharacterEquipement.get(BotItemType.SHIELD)));
		List<BotItemInfo> legArmorCharacter = sortItemsByLevel(
				filterOdd(equipableCharacterEquipement.get(BotItemType.LEG_ARMOR)));
		List<BotItemInfo> amulerCharacter = sortItemsByLevel(
				filterOdd(equipableCharacterEquipement.get(BotItemType.AMULET)));
		List<BotItemInfo> ring1Character = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.RING))));
		List<BotItemInfo> utility1Character = new LinkedList<>(
				sortItemsByLevel(equipableCharacterEquipement.get(BotItemType.UTILITY)));
		List<BotItemInfo> artifact1Character = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.ARTIFACT))));

		// On ajoute null pour les items multiples, � faire sur les autres si la notion
		// d'item mauvais apparait
		if (ring1Character.size() == 1 && ring1Character.getFirst().quantity() == 1) {
			addNullValueIfAbsent(ring1Character);
		}
		addNullValueIfAbsent(utility1Character);
		if (artifact1Character.size() < 3) {
			addNullValueIfAbsent(artifact1Character);
		}

		List<BotItemInfo> ring2Character = new LinkedList<>(ring1Character);
		List<BotItemInfo> utility2Character = new LinkedList<>(utility1Character);
		List<BotItemInfo> artifact2Character = new LinkedList<>(artifact1Character);
		List<BotItemInfo> artifact3Character = new LinkedList<>(artifact1Character);

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

		BotItemInfo[] bestEquipements = null;
		FightDetails maxFightDetails = DEFAULT_FIGHT_DETAILS;
		Map<Integer, Integer> effectMap = resetEffectMap();
		for (BotItemInfo[] botItemInfos : combinator) {
			if (validCombinaison(botItemInfos)) {
				for (BotItemInfo botItemInfo : botItemInfos) {
					if (botItemInfo != null) {
						updateEffectInMap(effectMap, botItemInfo.botItemDetails(), botItemInfo.quantity());
					}
				}

				// Evaluation
				FightDetails currentFightDetails = optimizeVictory(effectMap, monster, characterHpWithoutEqt,
						maxFightDetails.characterTurn());
				if (currentFightDetails.characterTurn() < maxFightDetails.characterTurn() || (currentFightDetails
						.characterTurn() == maxFightDetails.characterTurn()
						&& ((currentFightDetails.restoreTurn() < maxFightDetails.restoreTurn()
								&& currentFightDetails.win())
								|| (currentFightDetails.characterLossHP() < maxFightDetails.characterLossHP())))) {
					maxFightDetails = currentFightDetails;
					bestEquipements = botItemInfos.clone();
					if (maxFightDetails.characterTurn() == 1 && currentFightDetails.restoreTurn() == 0) {
						// On a trouv� 1 solution id�ale, on arr�te la recherche
						break;
					}
				}
				effectMap = resetEffectMap();
			}
		}

		if (bestEquipements == null) {
			// Possible que si le perso � 0 �quipement.
			bestEquipements = new BotItemInfo[14];

			// Evaluation
			maxFightDetails = DEFAULT_FIGHT_DETAILS;
		}

		OptimizeResult result = new OptimizeResult(maxFightDetails, bestEquipements);
		optimizeCacheManager.add(key, result);
		if (maxFightDetails.win() && maxFightDetails.restoreTurn() == 0) {
			noUseUtilityMonsterCode.add(monster.getCode());
		}
		return result;
	}

	private List<BotItemInfo> sortItemsByLevel(List<BotItemInfo> filterOdd) {
		return filterOdd.stream()
				.sorted((a, b) -> Integer.compare(b.botItemDetails().getLevel(), a.botItemDetails().getLevel()))
				.toList();
	}

	private List<BotItemInfo> filterOdd(List<BotItemInfo> items) {
		List<BotItemInfo> filteredItem = items.stream()
				.filter(bii -> !oddItems.contains(bii.botItemDetails().getCode())).toList();
		Map<String, Map<Integer, Integer>> itemsMap = new HashMap<>();
		for (BotItemInfo item : filteredItem) {
			if (!item.botItemDetails().getType().equals(BotItemType.RING) || item.quantity() > 1) {
				Map<Integer, Integer> effectMap = resetEffectMap();
				updateEffectInMap(effectMap, item.botItemDetails(), 1);
				itemsMap.put(item.botItemDetails().getCode(), effectMap);
			}
		}
		boolean oddItemAdded = false;
		for (BotItemInfo item : filteredItem) {
			String itemCode = item.botItemDetails().getCode();
			Map<Integer, Integer> effectMap = itemsMap.get(itemCode);
			if (effectMap != null) {
				for (Entry<String, Map<Integer, Integer>> entry : itemsMap.entrySet()) {
					if (!entry.getKey().equals(itemCode) && upperEffects(effectMap, entry.getValue())) {
						oddItems.add(itemCode);
						oddItemAdded = true;
						break;
					}
				}
			}
		}
		return oddItemAdded
				? filteredItem.stream().filter(bii -> !oddItems.contains(bii.botItemDetails().getCode())).toList()
				: filteredItem;
	}

	private boolean upperEffects(Map<Integer, Integer> effectMap, Map<Integer, Integer> effectMapToCompare) {
		for (Entry<Integer, Integer> entry : effectMap.entrySet()) {
			Integer effect = effectMapToCompare.get(entry.getKey());
			if (effect == null || entry.getValue() > effect) {
				return false;
			}
		}
		return true;
	}

	private void addNullValueIfAbsent(List<BotItemInfo> botItemList) {
		if (!botItemList.isEmpty() && !botItemList.contains(null)) {
			botItemList.add(null);
		}
	}

	@Override
	public FightDetails calculateFightResult(BotMonster monster) {
		int characterHp = characterService.getCharacterHPWithoutEquipment();
		return initOptimizeResultWithEquipedItems(characterDao.getCharacter(), monster, characterHp);
	}

	private BotItemInfo[] initBestEquipments(BotCharacter character) {
		return new BotItemInfo[] { initBestEquipement(character.getWeaponSlot(), 1),
				initBestEquipement(character.getBodyArmorSlot(), 1), initBestEquipement(character.getBootsSlot(), 1),
				initBestEquipement(character.getHelmetSlot(), 1), initBestEquipement(character.getShieldSlot(), 1),
				initBestEquipement(character.getLegArmorSlot(), 1), initBestEquipement(character.getAmuletSlot(), 1),
				initBestEquipement(character.getRing1Slot(), 1), initBestEquipement(character.getRing2Slot(), 1),
				initBestEquipement(character.getUtility1Slot(), character.getUtility1SlotQuantity()),
				initBestEquipement(character.getUtility2Slot(), character.getUtility2SlotQuantity()),
				initBestEquipement(character.getArtifact1Slot(), 1),
				initBestEquipement(character.getArtifact2Slot(), 1),
				initBestEquipement(character.getArtifact3Slot(), 1) };
	}

	private BotItemInfo initBestEquipement(String slot, int quantity) {
		return "".equals(slot) ? null : new BotItemInfo(itemDAO.getItem(slot), quantity);
	}

	private FightDetails initOptimizeResultWithEquipedItems(BotCharacter character, BotMonster monster,
			int characterHp) {
		Map<Integer, Integer> effectMap = resetEffectMap();

		updateEffectInMapForEquipedEqt(character.getWeaponSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getBodyArmorSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getBootsSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getHelmetSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getShieldSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getLegArmorSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getAmuletSlot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getRing1Slot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getRing2Slot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getUtility1Slot(), effectMap, character.getUtility1SlotQuantity());
		updateEffectInMapForEquipedEqt(character.getUtility2Slot(), effectMap, character.getUtility2SlotQuantity());
		updateEffectInMapForEquipedEqt(character.getArtifact1Slot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getArtifact2Slot(), effectMap, 1);
		updateEffectInMapForEquipedEqt(character.getArtifact3Slot(), effectMap, 1);

		return optimizeVictory(effectMap, monster, characterHp, GameConstants.MAX_FIGHT_TURN);
	}

	private void updateEffectInMapForEquipedEqt(String slot, Map<Integer, Integer> effectMap, int quantity) {
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

		// cas ou les rings sont dans l'inventaire, � la bank ou les 2
		if (botItemInfos[7] != null && botItemInfos[8] != null
				&& botItemInfos[7].botItemDetails().getCode().equals(botItemInfos[8].botItemDetails().getCode())) {
			return botItemInfos[7].quantity() > 1;
		}
		return true;
	}

	private void updateEffectInMap(Map<Integer, Integer> effectMap, BotItemDetails botItemDetail, int quantity) {
		botItemDetail.getEffects().stream().forEach(effect -> {
			if (BotEffect.RESTORE.equals(effect.getName())) {
				addRestore(effectMap, effect.getValue(), quantity);
			} else {
				effectMap.put(effect.getName().ordinal(),
						effect.getValue() + effectMap.getOrDefault(effect.getName().ordinal(), 0));
			}
		});
	}

	// On ignore les blocks
	private FightDetails optimizeVictory(Map<Integer, Integer> effectMap, BotMonster monster, int characterHp,
			int maxCharacterTurn) {
		int characterDmg = calculCharacterDamage(effectMap, monster);
		int characterTurn = calculTurns(monster.getHp(), characterDmg);
		if (characterTurn >= GameConstants.MAX_FIGHT_TURN) {
			// l'hypoth�se c'est que l'on ne doit pas se retrouver dans ce cas, d'ou hp
			// character � -1
			return DEFAULT_FIGHT_DETAILS;
		}
		if (characterTurn > maxCharacterTurn) {
			// IL y a mieux donc on envoi un r�sultat par d�faut
			return new FightDetails(false, characterTurn, characterTurn, Integer.MAX_VALUE, 0);
		}

		MonsterCalculStruct monsterResult = calculMonsterTurns(characterHp, effectMap, monster, characterTurn,
				characterDmg);
		// Le calcul fait que l'on privil�gie la non utilisation de potion quand cela
		// est possible
		int nbTurn = Math.min(characterTurn, monsterResult.monsterTurn());
		// Si le tour du monstre est inf�rieur � celui du perso on met 100 pour explorer
		// d'autres possibilit�s
		return new FightDetails(monsterResult.monsterTurn() >= characterTurn, nbTurn,
				nbTurn < characterTurn ? GameConstants.MAX_FIGHT_TURN : characterTurn,
				nbTurn < characterTurn ? Integer.MAX_VALUE : monsterResult.characterLossHP(),
				monsterResult.restoreTurn());
	}

	private List<RestoreStruct> getRestoreValue(Map<Integer, Integer> effectMap) {
		List<RestoreStruct> result = new LinkedList<>();
		int index = -1;
		while (effectMap.containsKey(index)) {
			result.add(new RestoreStruct(effectMap.get(index), effectMap.get(index - 1)));
			index -= 2;
		}
		return result;
	}

	private void addRestore(Map<Integer, Integer> effectMap, int value, int quantity) {
		int index = -1;
		while (effectMap.containsKey(index)) {
			index -= 2;
		}
		effectMap.put(index, value);
		effectMap.put(index - 1, quantity);
	}

	private int calculTurns(int hp, int dmg) {
		return dmg == 0 ? GameConstants.MAX_FIGHT_TURN : hp / dmg + (hp % dmg == 0 ? 0 : 1);
	}

	private MonsterCalculStruct calculMonsterTurns(int characterHp, Map<Integer, Integer> effectMap, BotMonster monster,
			int maxCharacterTurn, int characterDmg) {
		List<RestoreStruct> restoreValues = getRestoreValue(effectMap);
		int monsterDmg = calculMonsterDamage(effectMap, monster);
		int characterMaxHp = characterHp + effectMap.getOrDefault(BotEffect.HP.ordinal(), 0);
		int characterMaxHpWithBoost = characterMaxHp + effectMap.getOrDefault(BotEffect.BOOST_HP.ordinal(), 0);
		int halfCharacterMaxHpWithBoost = characterMaxHpWithBoost / 2;
		if (restoreValues.isEmpty()) {
			int monsterTurn = calculTurns(characterMaxHpWithBoost, calculMonsterDamage(effectMap, monster));

			int monsterTotalDmg = monsterDmg * (monsterTurn > maxCharacterTurn ? (maxCharacterTurn - 1) : monsterTurn);
			return new MonsterCalculStruct(monsterTurn, 0,
					Math.max(0, monsterTotalDmg - (characterMaxHpWithBoost - characterMaxHp)));
		}
		int halfMonsterTurn = calculTurns(halfCharacterMaxHpWithBoost, calculMonsterDamage(effectMap, monster));
		if (halfMonsterTurn >= maxCharacterTurn) {
			return new MonsterCalculStruct(halfMonsterTurn * 2, 0,
					Math.max(0, (maxCharacterTurn - 1) * monsterDmg - (characterMaxHpWithBoost - characterMaxHp)));
		}
		int monsterTurn = halfMonsterTurn;
		int characterHP = characterMaxHpWithBoost - halfMonsterTurn * monsterDmg;
		int monsterHP = monster.getHp() - halfMonsterTurn * characterDmg;
		int restoreTurn = 0;
		while (characterHP >= 0 && monsterHP >= 0) {
			if (characterHP < halfCharacterMaxHpWithBoost) {
				int restoreValue = getRestoreValue(restoreValues, restoreTurn);
				if (restoreValue > 0) {
					restoreTurn++;
				}
				characterHP += restoreValue;
			}
			monsterTurn++;
			monsterHP -= characterDmg;
			if (monsterHP > 0) {
				characterHP -= monsterDmg;
			}
		}
		return new MonsterCalculStruct(monsterTurn, restoreTurn, Math.max(0, (characterMaxHp - characterHP)));
	}

	private int getRestoreValue(List<RestoreStruct> restoreValues, int restoreTurn) {
		return restoreValues.stream().filter(rs -> rs.quantity() > restoreTurn).map(RestoreStruct::value).reduce(0,
				(a, b) -> a + b);
	}

	private Map<Integer, Integer> resetEffectMap() {
		return HashMap.newHashMap(BotEffect.values().length);
	}

	private int calculMonsterDamage(Map<Integer, Integer> effectMap, BotMonster monster) {
		int monsterEartDmg = monster.getAttackEarth()
				* (int) Math.rint(1 - (effectMap.getOrDefault(BotEffect.RES_EARTH.ordinal(), 0)
						+ effectMap.getOrDefault(BotEffect.BOOST_RES_EARTH.ordinal(), 0)) / 100f);
		int monsterAirDmg = monster.getAttackAir()
				* (int) Math.rint(1 - (effectMap.getOrDefault(BotEffect.RES_AIR.ordinal(), 0)
						+ effectMap.getOrDefault(BotEffect.BOOST_RES_AIR.ordinal(), 0)) / 100f);
		int monsterWaterDmg = monster.getAttackWater()
				* (int) Math.rint(1 - (effectMap.getOrDefault(BotEffect.RES_WATER.ordinal(), 0)
						+ effectMap.getOrDefault(BotEffect.BOOST_RES_WATER.ordinal(), 0)) / 100f);
		int monsterFireDmg = monster.getAttackFire()
				* (int) Math.rint(1 - (effectMap.getOrDefault(BotEffect.RES_FIRE.ordinal(), 0)
						+ effectMap.getOrDefault(BotEffect.BOOST_RES_FIRE.ordinal(), 0)) / 100f);
		return monsterEartDmg + monsterAirDmg + monsterWaterDmg + monsterFireDmg;
	}

	private int calculCharacterDamage(Map<Integer, Integer> effectMap, BotMonster monster) {
		int characterEartDmg = calculEffectDamage(effectMap.getOrDefault(BotEffect.ATTACK_EARTH.ordinal(), 0),
				effectMap.getOrDefault(BotEffect.DMG_EARTH.ordinal(), 0),
				effectMap.getOrDefault(BotEffect.BOOST_DMG_EARTH.ordinal(), 0), monster.getResEarth());
		int characterAirDmg = calculEffectDamage(effectMap.getOrDefault(BotEffect.ATTACK_AIR.ordinal(), 0),
				effectMap.getOrDefault(BotEffect.DMG_AIR.ordinal(), 0),
				effectMap.getOrDefault(BotEffect.BOOST_DMG_AIR.ordinal(), 0), monster.getResAir());
		int characterWaterDmg = calculEffectDamage(effectMap.getOrDefault(BotEffect.ATTACK_WATER.ordinal(), 0),
				effectMap.getOrDefault(BotEffect.DMG_WATER.ordinal(), 0),
				effectMap.getOrDefault(BotEffect.BOOST_DMG_WATER.ordinal(), 0), monster.getResWater());
		int characterFireDmg = calculEffectDamage(effectMap.getOrDefault(BotEffect.ATTACK_FIRE.ordinal(), 0),
				effectMap.getOrDefault(BotEffect.DMG_FIRE.ordinal(), 0),
				effectMap.getOrDefault(BotEffect.BOOST_DMG_FIRE.ordinal(), 0), monster.getResFire());
		return characterEartDmg + characterAirDmg + characterWaterDmg + characterFireDmg;
	}

	private int calculEffectDamage(float attackDmg, float dmgPercent, float dmgBoost, float monsterRes) {
		return (int) Math.rint((attackDmg * ((100d + dmgPercent + dmgBoost) * (100d - monsterRes)) / 10000));
	}

	private static final record MonsterCalculStruct(int monsterTurn, int restoreTurn, int characterLossHP) {
	}

	private static final record RestoreStruct(int value, int quantity) {
	}
}
