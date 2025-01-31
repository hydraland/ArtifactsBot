package strategy.util.fight;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
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
	private static final long MAX_COMBINATORICS_BEFORE_ACTIVATE_REDUCTION = 10000;
	private static final long MAX_COMBINATORICS_TO_ACTIVATE_REDUCTION = 1000000;
	private final CharacterDAO characterDao;
	private final CharacterService characterService;
	private final BankDAO bankDao;
	private final CacheManager<String, OptimizeResult> optimizeCacheManager;
	private final ItemService itemService;
	private final Set<String> oddItems;
	private final EffectServiceImpl effectService;
	private final CombinatoricsReducerImpl combinatoricsReducer;
	private final FightEvaluatorImpl fightEvaluator;

	public FightServiceImpl(CharacterDAO characterDao, BankDAO bankDao, ItemDAO itemDAO,
			CharacterService characterService, ItemService itemService) {
		this.characterDao = characterDao;
		this.bankDao = bankDao;
		this.characterService = characterService;
		this.itemService = itemService;
		// 1 semaine 3600*168
		this.optimizeCacheManager = new LimitedTimeCacheManager<>(3600 * 168);
		this.oddItems = new HashSet<>();
		this.effectService = new EffectServiceImpl();
		this.combinatoricsReducer = new CombinatoricsReducerImpl(effectService,
				MAX_COMBINATORICS_BEFORE_ACTIVATE_REDUCTION, MAX_COMBINATORICS_TO_ACTIVATE_REDUCTION);
		this.fightEvaluator = new FightEvaluatorImpl(effectService, itemDAO);
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

		Set<BotItemInfo> weapons = new HashSet<>(filterOdd(equipableCharacterEquipement.get(BotItemType.WEAPON)));
		Set<BotItemInfo> bodyArmors = new HashSet<>(
				filterOdd(equipableCharacterEquipement.get(BotItemType.BODY_ARMOR)));
		Set<BotItemInfo> boots = new HashSet<>(filterOdd(equipableCharacterEquipement.get(BotItemType.BOOTS)));
		Set<BotItemInfo> helmets = new HashSet<>(filterOdd(equipableCharacterEquipement.get(BotItemType.HELMET)));
		Set<BotItemInfo> shields = new HashSet<>(filterOdd(equipableCharacterEquipement.get(BotItemType.SHIELD)));
		Set<BotItemInfo> legArmors = new HashSet<>(filterOdd(equipableCharacterEquipement.get(BotItemType.LEG_ARMOR)));
		Set<BotItemInfo> amulets = new HashSet<>(filterOdd(equipableCharacterEquipement.get(BotItemType.AMULET)));
		Set<BotItemInfo> rings1 = new HashSet<>(filterOdd(equipableCharacterEquipement.get(BotItemType.RING)));
		Set<BotItemInfo> utilities1 = new HashSet<>(equipableCharacterEquipement.get(BotItemType.UTILITY));
		Set<BotItemInfo> artifacts1 = new HashSet<>(filterOdd(equipableCharacterEquipement.get(BotItemType.ARTIFACT)));

		fightEvaluator.init(characterDao.getCharacter(), monster, characterHpWithoutEqt, !utilities1.isEmpty());
		OptimizeResult result = fightEvaluator.evaluate();
		if (!(result.fightDetails().characterTurn() == 1 && result.bestEqt()[OptimizeResult.UTILITY1_INDEX] == null
				&& result.bestEqt()[OptimizeResult.UTILITY2_INDEX] == null)) {
			// Les items equipés ne sont pas 1 solution idéale, on lance la recherche
			if (FightService.isCombinatoricsTooHigh(MAX_COMBINATORICS_TO_ACTIVATE_REDUCTION, weapons.size(),
					bodyArmors.size(), boots.size(), helmets.size(), shields.size(), legArmors.size(), amulets.size(),
					rings1.size(), rings1.size(), artifacts1.size(), artifacts1.size(), artifacts1.size(),
					utilities1.size(), utilities1.size())) {
				combinatoricsReducer.reduceCombinatorics(weapons, bodyArmors, boots, helmets, shields, legArmors,
						amulets, rings1, artifacts1, utilities1, monster, characterHpWithoutEqt);
			}

			// On ajoute null pour les items multiples, à faire sur les autres si la notion
			// d'item mauvais apparait
			if (rings1.size() == 1 && rings1.stream().findFirst().get().quantity() == 1) {
				FightService.addNullValueIfAbsent(rings1);
			}
			FightService.addNullValueIfAbsent(utilities1);
			if (artifacts1.size() < 3) {
				FightService.addNullValueIfAbsent(artifacts1);
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

			result = fightEvaluator.evaluate(combinator);
		}

		addUsefullArtifacts(result.bestEqt());
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

	private List<BotItemInfo> filterOdd(List<BotItemInfo> items) {
		List<BotItemInfo> filteredItem = items.stream()
				.filter(bii -> !oddItems.contains(bii.botItemDetails().getCode())).toList();
		Map<String, ItemEffects> itemsMap = new HashMap<>();
		for (BotItemInfo item : filteredItem) {
			if (!item.botItemDetails().getType().equals(BotItemType.RING) || item.quantity() > 1) {
				ItemEffects itemEffects = effectService.getEffects(item.botItemDetails());
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

	@Override
	public FightDetails calculateFightResult(BotMonster monster) {
		int characterHp = characterService.getCharacterHPWithoutEquipment();
		fightEvaluator.init(characterDao.getCharacter(), monster, characterHp, true);
		return fightEvaluator.evaluate().fightDetails();
	}
}