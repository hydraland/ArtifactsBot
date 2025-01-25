package strategy.util.fight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import util.PermanentCacheManager;

public final class FightServiceImpl implements FightService {
	private static final int MAX_COMBINATORICS_BEFORE_ACTIVATE_REDUCTION = 10000;
	private static final FightDetails DEFAULT_FIGHT_DETAILS = new FightDetails(false, GameConstants.MAX_FIGHT_TURN,
			GameConstants.MAX_FIGHT_TURN, Integer.MAX_VALUE, 0, 0, 0);
	private static final long MAX_COMBINATORICS_TO_ACTIVATE_REDUCTION = 1000000;
	private final CharacterDAO characterDao;
	private final CharacterService characterService;
	private final BankDAO bankDao;
	private final ItemDAO itemDAO;
	private final CacheManager<String, OptimizeResult> optimizeCacheManager;
	private final ItemService itemService;
	private final Set<String> oddItems;
	private final EffectCumulator effectsCumulator;
	private final CacheManager<String, ItemEffects> effectCacheManager;

	public FightServiceImpl(CharacterDAO characterDao, BankDAO bankDao, ItemDAO itemDAO,
			CharacterService characterService, ItemService itemService) {
		this.characterDao = characterDao;
		this.bankDao = bankDao;
		this.itemDAO = itemDAO;
		this.characterService = characterService;
		this.itemService = itemService;
		// 1 semaine 3600*168
		this.optimizeCacheManager = new LimitedTimeCacheManager<>(3600 * 168);
		this.oddItems = new HashSet<>();
		this.effectsCumulator = new EffectCumulatorImpl();
		this.effectCacheManager = new PermanentCacheManager<>();
	}

	@Override
	public OptimizeResult optimizeEquipementsInInventory(BotMonster monster, Map<String, Integer> reservedItems,
			boolean useUtilities) {
		Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement = characterService
				.getEquipableCharacterEquipement(reservedItems, useUtilities);
		return optimizeEquipements(monster, equipableCharacterEquipement);
	}

	@Override
	public OptimizeResult optimizeEquipementsPossesed(BotMonster monster, Map<String, Integer> reservedItems,
			boolean useUtilities) {
		Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement = getAllCharacterEquipments(reservedItems,
				useUtilities);
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
		// On ignore les items dépassé
		addIgnoreItems(ignoreItems, oddItems);
		Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement = characterService
				.getEquipableCharacterEquipement(ignoreItems, useUtility);
		// On ignore les équipements que l'on a dans l'inventaire ou sur le perso avec
		// la particularité des ring
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

		List<BotItemInfo> weapons = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.WEAPON))));
		List<BotItemInfo> bodyArmors = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.BODY_ARMOR))));
		List<BotItemInfo> boots = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.BOOTS))));
		List<BotItemInfo> helmets = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.HELMET))));
		List<BotItemInfo> shields = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.SHIELD))));
		List<BotItemInfo> legArmors = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.LEG_ARMOR))));
		List<BotItemInfo> amulets = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.AMULET))));
		List<BotItemInfo> rings1 = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.RING))));
		List<BotItemInfo> utilities1 = new LinkedList<>(
				sortItemsByLevel(equipableCharacterEquipement.get(BotItemType.UTILITY)));
		List<BotItemInfo> artifacts1 = new LinkedList<>(
				sortItemsByLevel(filterOdd(equipableCharacterEquipement.get(BotItemType.ARTIFACT))));

		BotItemInfo[] bestEquipements = initBestEquipments(characterDao.getCharacter(), !utilities1.isEmpty());
		FightDetails maxFightDetails = initOptimizeResultWithEquipedItems(characterDao.getCharacter(), monster,
				characterHpWithoutEqt, !utilities1.isEmpty());
		if (!(maxFightDetails.characterTurn() == 1 && bestEquipements[OptimizeResult.UTILITY1_INDEX] == null
				&& bestEquipements[OptimizeResult.UTILITY2_INDEX] == null)) {
			// Les items equipés ne sont pas 1 solution idéale, on lance la recherche

			if (isCombinatoricsTooHigh(MAX_COMBINATORICS_TO_ACTIVATE_REDUCTION, weapons.size(), bodyArmors.size(),
					boots.size(), helmets.size(), shields.size(), legArmors.size(), amulets.size(), rings1.size(),
					rings1.size(), artifacts1.size(), artifacts1.size(), artifacts1.size(), utilities1.size(),
					utilities1.size())) {
				reduceCombinatorics(weapons, bodyArmors, boots, helmets, shields, legArmors, amulets, rings1,
						artifacts1, utilities1, monster, characterHpWithoutEqt);
			}

			// On ajoute null pour les items multiples, à faire sur les autres si la notion
			// d'item mauvais apparait
			if (rings1.size() == 1 && rings1.getFirst().quantity() == 1) {
				addNullValueIfAbsent(rings1, true);
			}
			addNullValueIfAbsent(utilities1, false);
			if (artifacts1.size() < 3) {
				addNullValueIfAbsent(artifacts1, true);
			}

			List<BotItemInfo> rings2 = new LinkedList<>(rings1);
			List<BotItemInfo> utilities2 = new LinkedList<>(utilities1);
			List<BotItemInfo> artifacts2 = new LinkedList<>(artifacts1);
			List<BotItemInfo> artifacts3 = new LinkedList<>(artifacts1);

			Combinator<BotItemInfo> combinator = new Combinator<>(BotItemInfo.class, 14);
			combinator.set(OptimizeResult.UTILITY1_INDEX, utilities1);
			combinator.set(OptimizeResult.UTILITY2_INDEX, utilities2);
			combinator.set(OptimizeResult.WEAPON_INDEX, weapons);
			combinator.set(OptimizeResult.BODY_ARMOR_INDEX, bodyArmors);
			combinator.set(OptimizeResult.BOOTS_INDEX, boots);
			combinator.set(OptimizeResult.HELMET_INDEX, helmets);
			combinator.set(OptimizeResult.SHIELD_INDEX, shields);
			combinator.set(OptimizeResult.LEG_ARMOR_INDEX, legArmors);
			combinator.set(OptimizeResult.AMULET_INDEX, amulets);
			combinator.set(OptimizeResult.RING1_INDEX, rings1);
			combinator.set(OptimizeResult.RING2_INDEX, rings2);
			combinator.set(OptimizeResult.ARTIFACT1_INDEX, artifacts1);
			combinator.set(OptimizeResult.ARTIFACT2_INDEX, artifacts2);
			combinator.set(OptimizeResult.ARTIFACT3_INDEX, artifacts3);

			effectsCumulator.reset();
			for (BotItemInfo[] botItemInfos : combinator) {
				if (validCombinaison(botItemInfos, OptimizeResult.RING1_INDEX, OptimizeResult.RING2_INDEX,
						OptimizeResult.UTILITY1_INDEX, OptimizeResult.UTILITY2_INDEX, OptimizeResult.ARTIFACT1_INDEX,
						OptimizeResult.ARTIFACT2_INDEX, OptimizeResult.ARTIFACT3_INDEX)) {
					for (BotItemInfo botItemInfo : botItemInfos) {
						if (botItemInfo != null) {
							updateEffectsCumulator(botItemInfo.botItemDetails(), botItemInfo.quantity());
						}
					}

					// Evaluation
					FightDetails currentFightDetails = optimizeVictory(monster, characterHpWithoutEqt,
							maxFightDetails.characterTurn());

					// TODO sortir dans une fonction d'évaluation
					if ((!maxFightDetails.win() && !currentFightDetails.win()
							&& currentFightDetails.characterTurn() < maxFightDetails.characterTurn())
							|| ((!maxFightDetails.win() && currentFightDetails.win())
									|| ((!maxFightDetails.win() || currentFightDetails.win())
											&& currentFightDetails.characterTurn() < maxFightDetails.characterTurn())
									|| (currentFightDetails.win() && maxFightDetails.win()
											&& currentFightDetails.characterTurn() == maxFightDetails.characterTurn()
											&& ((currentFightDetails.restoreTurn() < maxFightDetails.restoreTurn())
													|| (currentFightDetails.restoreTurn() == maxFightDetails
															.restoreTurn()
															&& currentFightDetails.characterLossHP() < maxFightDetails
																	.characterLossHP()))))) {
						maxFightDetails = currentFightDetails;
						bestEquipements = botItemInfos.clone();
						if (maxFightDetails.characterTurn() == 1
								&& bestEquipements[OptimizeResult.UTILITY1_INDEX] == null
								&& bestEquipements[OptimizeResult.UTILITY2_INDEX] == null) {
							// On a trouvé 1 solution idéale, on arrête la recherche
							break;
						}
					}
					effectsCumulator.reset();
				}
			}
		}

		addUsefullArtifacts(bestEquipements);

		OptimizeResult result = new OptimizeResult(maxFightDetails, bestEquipements);
		optimizeCacheManager.add(key, result);
		return result;
	}

	private void addUsefullArtifacts(BotItemInfo[] bestEquipements) {
		if (bestEquipements[OptimizeResult.ARTIFACT1_INDEX] == null
				|| bestEquipements[OptimizeResult.ARTIFACT2_INDEX] == null
				|| bestEquipements[OptimizeResult.ARTIFACT3_INDEX] == null) {
			List<String> bestArtifactsFound = new LinkedList<>();
			for (int i : new int[] { OptimizeResult.ARTIFACT1_INDEX, OptimizeResult.ARTIFACT2_INDEX,
					OptimizeResult.ARTIFACT3_INDEX }) {
				if (bestEquipements[i] != null) {
					bestArtifactsFound.add(bestEquipements[i].botItemDetails().getCode());
				}
			}
			List<BotItemDetails> usefullArtifacts = itemService.getUsefullArtifacts();
			List<BotItemInfo> artifactsEquipable = new LinkedList<>();
			for (BotItemDetails artifact : usefullArtifacts) {
				if (characterService.isPossess(artifact.getCode(), bankDao)
						&& !bestArtifactsFound.contains(artifact.getCode())) {
					artifactsEquipable.add(new BotItemInfo(artifact, 1));
				}
			}

			for (BotItemInfo artifactInfo : artifactsEquipable) {
				if (bestEquipements[OptimizeResult.ARTIFACT1_INDEX] == null) {
					bestEquipements[OptimizeResult.ARTIFACT1_INDEX] = artifactInfo;
				} else if (bestEquipements[OptimizeResult.ARTIFACT2_INDEX] == null) {
					bestEquipements[OptimizeResult.ARTIFACT2_INDEX] = artifactInfo;
				} else if (bestEquipements[OptimizeResult.ARTIFACT3_INDEX] == null) {
					bestEquipements[OptimizeResult.ARTIFACT3_INDEX] = artifactInfo;
				}
			}
		}
	}

	// TODO mettre dans une classe et mettre index en constante
	private void reduceCombinatorics(List<BotItemInfo> weapons, List<BotItemInfo> bodyArmors, List<BotItemInfo> boots,
			List<BotItemInfo> helmets, List<BotItemInfo> shields, List<BotItemInfo> legArmors,
			List<BotItemInfo> amulets, List<BotItemInfo> rings, List<BotItemInfo> artifacts,
			List<BotItemInfo> utilities, BotMonster monster, int characterHpWithoutEqt) {

		List<List<BotItemInfo>> sources = Arrays.asList(weapons, bodyArmors, amulets, helmets, legArmors, rings, boots,
				shields, artifacts, utilities);
		if (rings.size() == 1 && rings.getFirst().quantity() == 1) {
			addNullValueIfAbsent(rings, true);
		}
		if (artifacts.size() < 3) {
			addNullValueIfAbsent(artifacts, true);
		}

		// Séparation des utilities en restore et autre
		List<BotItemInfo> restoreUtilities = new LinkedList<>();
		for (Iterator<BotItemInfo> iterator = utilities.iterator(); iterator.hasNext();) {
			BotItemInfo botItemInfo = iterator.next();
			if (botItemInfo.botItemDetails().getEffects().stream()
					.anyMatch(bie -> BotEffect.RESTORE.equals(bie.getName()))) {
				restoreUtilities.add(botItemInfo);
				iterator.remove();
			}
		}
		addNullValueIfAbsent(utilities, false);

		List<Set<BotItemInfo>> tempList = new ArrayList<>();
		List<Set<BotItemInfo>> resultList = new ArrayList<>();
		for (int i = 0; i < sources.size(); i++) {
			if (i == 5 || i == 9) {
				tempList.add(new HashSet<>());
				resultList.add(new HashSet<>(sources.get(i)));
			} else if (i == 8) {
				tempList.add(new HashSet<>());
				tempList.add(new HashSet<>());
				resultList.add(new HashSet<>(sources.get(i)));
				resultList.add(new HashSet<>(sources.get(i)));
			}
			tempList.add(new HashSet<>());
			resultList.add(new HashSet<>(sources.get(i)));
		}
		int maxEvaluate = 0;
		for (int i = 0; i < resultList.size(); i++) {
			Combinator<BotItemInfo> combinator = new Combinator<>(BotItemInfo.class, 14);
			combinator.set(0, i == 0 ? sources.get(0) : resultList.get(0));
			combinator.set(1, i == 1 ? sources.get(1) : (i < 1 ? Collections.emptyList() : resultList.get(1)));
			combinator.set(2, i == 2 ? sources.get(2) : (i < 2 ? Collections.emptyList() : resultList.get(2)));
			combinator.set(3, i == 3 ? sources.get(3) : (i < 3 ? Collections.emptyList() : resultList.get(3)));
			combinator.set(4, i == 4 ? sources.get(4) : (i < 4 ? Collections.emptyList() : resultList.get(4)));
			combinator.set(5, i == 5 ? sources.get(5) : (i < 5 ? Collections.emptyList() : resultList.get(5)));
			combinator.set(6, i == 6 ? sources.get(5) : (i < 6 ? Collections.emptyList() : resultList.get(6)));
			combinator.set(7, i == 7 ? sources.get(6) : (i < 7 ? Collections.emptyList() : resultList.get(7)));
			combinator.set(8, i == 8 ? sources.get(7) : (i < 8 ? Collections.emptyList() : resultList.get(8)));
			combinator.set(9, i == 9 ? sources.get(8) : (i < 9 ? Collections.emptyList() : resultList.get(9)));
			combinator.set(10, i == 10 ? sources.get(8) : (i < 10 ? Collections.emptyList() : resultList.get(10)));
			combinator.set(11, i == 11 ? sources.get(8) : (i < 11 ? Collections.emptyList() : resultList.get(11)));
			combinator.set(12, i == 12 ? sources.get(9) : (i < 12 ? Collections.emptyList() : resultList.get(12)));
			combinator.set(13, i == 13 ? sources.get(9) : (i < 13 ? Collections.emptyList() : resultList.get(13)));

			if (!isCombinatoricsTooHigh(MAX_COMBINATORICS_BEFORE_ACTIVATE_REDUCTION, combinator.size(0),
					combinator.size(1), combinator.size(2), combinator.size(3), combinator.size(4), combinator.size(5),
					combinator.size(6), combinator.size(7), combinator.size(8), combinator.size(9), combinator.size(10),
					combinator.size(11), combinator.size(12), combinator.size(13))) {
				continue;
			}
			boolean combinatoricsTooHigh = isCombinatoricsTooHigh(MAX_COMBINATORICS_TO_ACTIVATE_REDUCTION,
					combinator.size(0), combinator.size(1), combinator.size(2), combinator.size(3), combinator.size(4),
					combinator.size(5), combinator.size(6), combinator.size(7), combinator.size(8), combinator.size(9),
					combinator.size(10), combinator.size(11), combinator.size(12), combinator.size(13));
			maxEvaluate = i;
			FightDetails maxFightDetails = DEFAULT_FIGHT_DETAILS;
			effectsCumulator.reset();
			Set<BotItemInfo> itemsSetTemp;
			for (BotItemInfo[] botItemInfos : combinator) {
				if (validCombinaison(botItemInfos, 5, 6, 9, 10, 11, 12, 13)) {
					for (BotItemInfo botItemInfo : botItemInfos) {
						if (botItemInfo != null) {
							updateEffectsCumulator(botItemInfo.botItemDetails(), botItemInfo.quantity());
						}
					}

					// Evaluation
					FightDetails currentFightDetails = optimizeVictory(monster, characterHpWithoutEqt,
							maxFightDetails.characterTurn());
					// Hypothèse aucun équipement ne fait de récupération de PV
					if (currentFightDetails.characterTurn() < maxFightDetails.characterTurn() || (combinatoricsTooHigh
							&& currentFightDetails.characterTurn() == maxFightDetails.characterTurn()
							&& (currentFightDetails.diffPower() > maxFightDetails.diffPower()))) {
						maxFightDetails = currentFightDetails;
						for (int j = 0; j <= i; j++) {
							itemsSetTemp = tempList.get(j);
							itemsSetTemp.clear();
							if (botItemInfos[j] != null) {
								itemsSetTemp.add(botItemInfos[j]);
							}
						}
						if (maxFightDetails.characterTurn() == 1 && botItemInfos[12] == null
								&& botItemInfos[13] == null) {
							// On a trouvé 1 solution idéale, on arrête la recherche
							break;
						}
					} else if ((currentFightDetails.characterTurn() == maxFightDetails.characterTurn())
							&& (!combinatoricsTooHigh
									|| (currentFightDetails.diffPower() == maxFightDetails.diffPower()))) {
						for (int j = 0; j <= i; j++) {
							if (botItemInfos[j] != null) {
								itemsSetTemp = tempList.get(j);
								itemsSetTemp.add(botItemInfos[j]);
							}
						}
					}
					effectsCumulator.reset();
				}
			}

			for (int j = 0; j <= i; j++) {
				itemsSetTemp = resultList.get(j);
				itemsSetTemp.clear();
				itemsSetTemp.addAll(tempList.get(j));
			}
		}
		for (int j = 0; j <= maxEvaluate && j < sources.size(); j++) {
			List<BotItemInfo> itemsSetTemp = sources.get(j);
			itemsSetTemp.clear();
			switch (j) {
			case 5 -> {
				itemsSetTemp.addAll(resultList.get(5));
				itemsSetTemp.addAll(resultList.get(6));
			}
			case 6 -> itemsSetTemp.addAll(resultList.get(7));
			case 7 -> itemsSetTemp.addAll(resultList.get(8));
			case 8 -> {
				itemsSetTemp.addAll(resultList.get(9));
				itemsSetTemp.addAll(resultList.get(10));
				itemsSetTemp.addAll(resultList.get(11));
			}
			case 9 -> {
				itemsSetTemp.addAll(resultList.get(12));
				itemsSetTemp.addAll(resultList.get(13));
				itemsSetTemp.addAll(restoreUtilities);
			}
			default -> itemsSetTemp.addAll(resultList.get(j));
			}
		}
	}

	private boolean isCombinatoricsTooHigh(long maxCombinatoricsValue, int... values) {
		long currentVal = 1l;
		for (int value : values) {
			currentVal *= value == 0 ? 1 : value;
			if (currentVal > maxCombinatoricsValue) {
				return true;
			}
		}
		return false;
	}

	private List<BotItemInfo> sortItemsByLevel(List<BotItemInfo> filterOdd) {
		return filterOdd.stream()
				.sorted((a, b) -> Integer.compare(b.botItemDetails().getLevel(), a.botItemDetails().getLevel()))
				.toList();
	}

	private List<BotItemInfo> filterOdd(List<BotItemInfo> items) {
		List<BotItemInfo> filteredItem = items.stream()
				.filter(bii -> !oddItems.contains(bii.botItemDetails().getCode())).toList();
		Map<String, ItemEffects> itemsMap = new HashMap<>();
		for (BotItemInfo item : filteredItem) {
			if (!item.botItemDetails().getType().equals(BotItemType.RING) || item.quantity() > 1) {
				ItemEffects itemEffects = getEffects(item.botItemDetails());
				itemsMap.put(item.botItemDetails().getCode(), itemEffects);
			}
		}
		boolean oddItemAdded = false;
		for (BotItemInfo item : filteredItem) {
			String itemCode = item.botItemDetails().getCode();
			ItemEffects itemEffects = itemsMap.get(itemCode);
			if (itemEffects != null) {
				for (Entry<String, ItemEffects> entry : itemsMap.entrySet()) {
					if (!entry.getKey().equals(itemCode) && entry.getValue().isUpper(itemEffects)) {
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

	private void addNullValueIfAbsent(List<BotItemInfo> botItemList, boolean after) {
		if (!botItemList.isEmpty() && !botItemList.contains(null)) {
			if (after) {
				botItemList.addLast(null);
			} else {
				botItemList.addFirst(null);
			}
		}
	}

	@Override
	public FightDetails calculateFightResult(BotMonster monster) {
		int characterHp = characterService.getCharacterHPWithoutEquipment();
		return initOptimizeResultWithEquipedItems(characterDao.getCharacter(), monster, characterHp, true);
	}

	private BotItemInfo[] initBestEquipments(BotCharacter character, boolean useUtilities) {
		return new BotItemInfo[] {
				useUtilities ? initBestEquipement(character.getUtility1Slot(), character.getUtility1SlotQuantity())
						: null,
				useUtilities ? initBestEquipement(character.getUtility2Slot(), character.getUtility2SlotQuantity())
						: null,
				initBestEquipement(character.getWeaponSlot(), 1), initBestEquipement(character.getBodyArmorSlot(), 1),
				initBestEquipement(character.getBootsSlot(), 1), initBestEquipement(character.getHelmetSlot(), 1),
				initBestEquipement(character.getShieldSlot(), 1), initBestEquipement(character.getLegArmorSlot(), 1),
				initBestEquipement(character.getAmuletSlot(), 1), initBestEquipement(character.getRing1Slot(), 1),
				initBestEquipement(character.getRing2Slot(), 1),

				initBestEquipement(character.getArtifact1Slot(), 1),
				initBestEquipement(character.getArtifact2Slot(), 1),
				initBestEquipement(character.getArtifact3Slot(), 1) };
	}

	private BotItemInfo initBestEquipement(String slot, int quantity) {
		return "".equals(slot) ? null : new BotItemInfo(itemDAO.getItem(slot), quantity);
	}

	private FightDetails initOptimizeResultWithEquipedItems(BotCharacter character, BotMonster monster, int characterHp,
			boolean useUtilities) {
		effectsCumulator.reset();

		updateEffectInMapForEquipedEqt(character.getWeaponSlot(), 1);
		updateEffectInMapForEquipedEqt(character.getBodyArmorSlot(), 1);
		updateEffectInMapForEquipedEqt(character.getBootsSlot(), 1);
		updateEffectInMapForEquipedEqt(character.getHelmetSlot(), 1);
		updateEffectInMapForEquipedEqt(character.getShieldSlot(), 1);
		updateEffectInMapForEquipedEqt(character.getLegArmorSlot(), 1);
		updateEffectInMapForEquipedEqt(character.getAmuletSlot(), 1);
		updateEffectInMapForEquipedEqt(character.getRing1Slot(), 1);
		updateEffectInMapForEquipedEqt(character.getRing2Slot(), 1);
		if (useUtilities) {
			updateEffectInMapForEquipedEqt(character.getUtility1Slot(), character.getUtility1SlotQuantity());
			updateEffectInMapForEquipedEqt(character.getUtility2Slot(), character.getUtility2SlotQuantity());
		}
		updateEffectInMapForEquipedEqt(character.getArtifact1Slot(), 1);
		updateEffectInMapForEquipedEqt(character.getArtifact2Slot(), 1);
		updateEffectInMapForEquipedEqt(character.getArtifact3Slot(), 1);

		return optimizeVictory(monster, characterHp, GameConstants.MAX_FIGHT_TURN);
	}

	private void updateEffectInMapForEquipedEqt(String slot, int quantity) {
		if (!"".equals(slot)) {
			updateEffectsCumulator(itemDAO.getItem(slot), quantity);
		}
	}

	private boolean validCombinaison(BotItemInfo[] botItemInfos, int ringIndex1, int ringIndex2,
			int... uniqueItemIndex) {
		Set<String> uniqueEquipItem = new HashSet<>();
		for (int i : uniqueItemIndex) {
			if (botItemInfos[i] != null) {
				if (uniqueEquipItem.contains(botItemInfos[i].botItemDetails().getCode())) {
					return false;
				} else {
					uniqueEquipItem.add(botItemInfos[i].botItemDetails().getCode());
				}
			}
		}

		// cas ou les rings sont dans l'inventaire, à la bank ou les 2
		if (botItemInfos[ringIndex1] != null && botItemInfos[ringIndex2] != null && botItemInfos[ringIndex1]
				.botItemDetails().getCode().equals(botItemInfos[ringIndex2].botItemDetails().getCode())) {
			return botItemInfos[ringIndex1].quantity() > 1;
		}
		return true;
	}

	private ItemEffects getEffects(BotItemDetails botItemDetail) {
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

	private void updateEffectsCumulator(BotItemDetails botItemDetail, int quantity) {
		effectsCumulator.accumulate(getEffects(botItemDetail), quantity);
	}

	// On ignore les blocks
	private FightDetails optimizeVictory(BotMonster monster, int characterHp, int maxCharacterTurn) {
		int characterDmg = calculCharacterDamage(monster);
		int characterTurn = calculTurns(monster.getHp(), characterDmg);
		if (characterTurn >= GameConstants.MAX_FIGHT_TURN) {
			// l'hypothèse c'est que l'on ne doit pas se retrouver dans ce cas, d'ou hp
			// character à -1
			return DEFAULT_FIGHT_DETAILS;
		}
		if (characterTurn > maxCharacterTurn) {
			// IL y a mieux donc on envoi un résultat par défaut
			return new FightDetails(false, characterTurn, characterTurn, Integer.MAX_VALUE, 0, 0, 0);
		}

		MonsterCalculStruct monsterResult = calculMonsterTurns(characterHp, monster, characterTurn, characterDmg);
		// Le calcul fait que l'on privilégie la non utilisation de potion quand cela
		// est possible
		int nbTurn = Math.min(characterTurn, monsterResult.monsterTurn());
		return new FightDetails(monsterResult.monsterTurn() >= characterTurn, nbTurn, characterTurn,
				monsterResult.characterLossHP(), monsterResult.restoreTurn(), characterDmg, monsterResult.monsterDmg());
	}

	private int calculTurns(int hp, int dmg) {
		return dmg == 0 ? GameConstants.MAX_FIGHT_TURN : hp / dmg + (hp % dmg == 0 ? 0 : 1);
	}

	private MonsterCalculStruct calculMonsterTurns(int characterHp, BotMonster monster, int maxCharacterTurn,
			int characterDmg) {
		int monsterDmg = calculMonsterDamage(monster);
		int characterMaxHp = characterHp + effectsCumulator.getEffectValue(BotEffect.HP);
		int characterMaxHpWithBoost = characterMaxHp + effectsCumulator.getEffectValue(BotEffect.BOOST_HP);
		int halfCharacterMaxHpWithBoost = characterMaxHpWithBoost / 2;
		if (!effectsCumulator.isRestore()) {
			int monsterTurn = calculTurns(characterMaxHpWithBoost, calculMonsterDamage(monster));

			int monsterTotalDmg = monsterDmg * (monsterTurn > maxCharacterTurn ? (maxCharacterTurn - 1) : monsterTurn);
			return new MonsterCalculStruct(monsterTurn, 0,
					Math.max(0, monsterTotalDmg - (characterMaxHpWithBoost - characterMaxHp)), monsterDmg);
		}
		int halfMonsterTurn = calculTurns(halfCharacterMaxHpWithBoost, calculMonsterDamage(monster));
		if (halfMonsterTurn >= maxCharacterTurn) {
			return new MonsterCalculStruct(halfMonsterTurn * 2, 0,
					Math.max(0, (maxCharacterTurn - 1) * monsterDmg - (characterMaxHpWithBoost - characterMaxHp)),
					monsterDmg);
		}
		int monsterTurn = halfMonsterTurn;
		int characterHP = characterMaxHpWithBoost - halfMonsterTurn * monsterDmg;
		int monsterHP = monster.getHp() - halfMonsterTurn * characterDmg;
		int restoreTurn = 1;
		while (characterHP >= 0 && monsterHP >= 0) {
			if (characterHP < halfCharacterMaxHpWithBoost) {
				int restoreValue = effectsCumulator.getRestoreEffectValue(restoreTurn);
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
		return new MonsterCalculStruct(monsterTurn, restoreTurn - 1, Math.max(0, (characterMaxHp - characterHP)),
				monsterDmg);
	}

	private int calculMonsterDamage(BotMonster monster) {
		int monsterEartDmg = (int) Math
				.rint(monster.getAttackEarth() * (1 - (effectsCumulator.getEffectValue(BotEffect.RES_EARTH)
						+ effectsCumulator.getEffectValue(BotEffect.BOOST_RES_EARTH)) / 100f));
		int monsterAirDmg = (int) Math
				.rint(monster.getAttackAir() * (1 - (effectsCumulator.getEffectValue(BotEffect.RES_AIR)
						+ effectsCumulator.getEffectValue(BotEffect.BOOST_RES_AIR)) / 100f));
		int monsterWaterDmg = (int) Math
				.rint(monster.getAttackWater() * (1 - (effectsCumulator.getEffectValue(BotEffect.RES_WATER)
						+ effectsCumulator.getEffectValue(BotEffect.BOOST_RES_WATER)) / 100f));
		int monsterFireDmg = (int) Math
				.rint(monster.getAttackFire() * (1 - (effectsCumulator.getEffectValue(BotEffect.RES_FIRE)
						+ effectsCumulator.getEffectValue(BotEffect.BOOST_RES_FIRE)) / 100f));
		return monsterEartDmg + monsterAirDmg + monsterWaterDmg + monsterFireDmg;
	}

	private int calculCharacterDamage(BotMonster monster) {
		int characterEartDmg = calculEffectDamage(effectsCumulator.getEffectValue(BotEffect.ATTACK_EARTH),
				effectsCumulator.getEffectValue(BotEffect.DMG_EARTH),
				effectsCumulator.getEffectValue(BotEffect.BOOST_DMG_EARTH), monster.getResEarth());
		int characterAirDmg = calculEffectDamage(effectsCumulator.getEffectValue(BotEffect.ATTACK_AIR),
				effectsCumulator.getEffectValue(BotEffect.DMG_AIR),
				effectsCumulator.getEffectValue(BotEffect.BOOST_DMG_AIR), monster.getResAir());
		int characterWaterDmg = calculEffectDamage(effectsCumulator.getEffectValue(BotEffect.ATTACK_WATER),
				effectsCumulator.getEffectValue(BotEffect.DMG_WATER),
				effectsCumulator.getEffectValue(BotEffect.BOOST_DMG_WATER), monster.getResWater());
		int characterFireDmg = calculEffectDamage(effectsCumulator.getEffectValue(BotEffect.ATTACK_FIRE),
				effectsCumulator.getEffectValue(BotEffect.DMG_FIRE),
				effectsCumulator.getEffectValue(BotEffect.BOOST_DMG_FIRE), monster.getResFire());
		return characterEartDmg + characterAirDmg + characterWaterDmg + characterFireDmg;
	}

	private int calculEffectDamage(float attackDmg, float dmgPercent, float dmgBoost, float monsterRes) {
		return (int) Math.rint((attackDmg * ((100d + dmgPercent + dmgBoost) * (100d - monsterRes)) / 10000));
	}

	private static final record MonsterCalculStruct(int monsterTurn, int restoreTurn, int characterLossHP,
			int monsterDmg) {
	}
}
