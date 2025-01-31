package strategy.util.fight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import hydra.model.BotEffect;
import hydra.model.BotMonster;
import strategy.util.BotItemInfo;
import util.Combinator;

public final class CombinatoricsReducerImpl implements CombinatoricsReducer {
	private static final int RESULT_UTILITY1_INDEX = 12;
	private static final int RESULT_UTILITY2_INDEX = 13;
	private static final int RESULT_WEAPON_INDEX = 0;
	private static final int RESULT_BODY_ARMOR_INDEX = 1;
	private static final int RESULT_BOOTS_INDEX = 7;
	private static final int RESULT_HELMET_INDEX = 3;
	private static final int RESULT_SHIELD_INDEX = 8;
	private static final int RESULT_LEG_ARMOR_INDEX = 4;
	private static final int RESULT_AMULET_INDEX = 2;
	private static final int RESULT_RING1_INDEX = 5;
	private static final int RESULT_RING2_INDEX = 6;
	private static final int RESULT_ARTIFACT1_INDEX = 9;
	private static final int RESULT_ARTIFACT2_INDEX = 10;
	private static final int RESULT_ARTIFACT3_INDEX = 11;

	private static final int SOURCE_UTILITY_INDEX = 9;
	private static final int SOURCE_WEAPON_INDEX = 0;
	private static final int SOURCE_BODY_ARMOR_INDEX = 1;
	private static final int SOURCE_BOOTS_INDEX = 6;
	private static final int SOURCE_HELMET_INDEX = 3;
	private static final int SOURCE_SHIELD_INDEX = 7;
	private static final int SOURCE_LEG_ARMOR_INDEX = 4;
	private static final int SOURCE_AMULET_INDEX = 2;
	private static final int SOURCE_RING_INDEX = 5;
	private static final int SOURCE_ARTIFACT_INDEX = 8;

	private final long maxCombinatoricBeforeActivateReduction;
	private final long maxCombinatoricToActivateReduction;
	private final TurnDiffPowerFightDetailsEvaluator combinatoricsFightDetailsEvaluator;
	private final EffectCumulator effectsCumulator;
	private final EffectService effectService;
	private final FightCalculator fightCalculator;

	public CombinatoricsReducerImpl(EffectService effectService, long maxCombinatoricBeforeActivateReduction,
			long maxCombinatoricToActivateReduction) {
		this.effectService = effectService;
		this.maxCombinatoricBeforeActivateReduction = maxCombinatoricBeforeActivateReduction;
		this.maxCombinatoricToActivateReduction = maxCombinatoricToActivateReduction;
		this.combinatoricsFightDetailsEvaluator = new TurnDiffPowerFightDetailsEvaluator();
		this.effectsCumulator = new EffectCumulatorImpl();
		this.fightCalculator = new FightCalculatorOptimizeTurn();
	}

	@Override
	public void reduceCombinatorics(Set<BotItemInfo> weapons, Set<BotItemInfo> bodyArmors, Set<BotItemInfo> boots,
			Set<BotItemInfo> helmets, Set<BotItemInfo> shields, Set<BotItemInfo> legArmors, Set<BotItemInfo> amulets,
			Set<BotItemInfo> rings, Set<BotItemInfo> artifacts, Set<BotItemInfo> utilities, BotMonster monster,
			int characterHpWithoutEqt) {

		List<Set<BotItemInfo>> sources = Arrays.asList(weapons, bodyArmors, amulets, helmets, legArmors, rings, boots,
				shields, artifacts, utilities);
		if (rings.size() == 1 && rings.stream().findFirst().get().quantity() == 1) {
			FightService.addNullValueIfAbsent(rings);
		}
		if (artifacts.size() < 3) {
			FightService.addNullValueIfAbsent(artifacts);
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
		FightService.addNullValueIfAbsent(utilities);

		List<Set<BotItemInfo>> tempList = new ArrayList<>();
		List<Set<BotItemInfo>> resultList = new ArrayList<>();
		for (int i = 0; i < sources.size(); i++) {
			if (i == SOURCE_RING_INDEX || i == SOURCE_UTILITY_INDEX) {
				tempList.add(new HashSet<>());
				resultList.add(new HashSet<>(sources.get(i)));
			} else if (i == SOURCE_ARTIFACT_INDEX) {
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
			combinator.set(RESULT_WEAPON_INDEX,
					i == RESULT_WEAPON_INDEX ? sources.get(SOURCE_WEAPON_INDEX) : resultList.get(RESULT_WEAPON_INDEX));
			combinator.set(RESULT_BODY_ARMOR_INDEX,
					i == RESULT_BODY_ARMOR_INDEX ? sources.get(SOURCE_BODY_ARMOR_INDEX)
							: (i < RESULT_BODY_ARMOR_INDEX ? Collections.emptyList()
									: resultList.get(RESULT_BODY_ARMOR_INDEX)));
			combinator.set(RESULT_AMULET_INDEX, i == RESULT_AMULET_INDEX ? sources.get(SOURCE_AMULET_INDEX)
					: (i < RESULT_AMULET_INDEX ? Collections.emptyList() : resultList.get(RESULT_AMULET_INDEX)));
			combinator.set(RESULT_HELMET_INDEX, i == RESULT_HELMET_INDEX ? sources.get(SOURCE_HELMET_INDEX)
					: (i < RESULT_HELMET_INDEX ? Collections.emptyList() : resultList.get(RESULT_HELMET_INDEX)));
			combinator.set(RESULT_LEG_ARMOR_INDEX, i == RESULT_LEG_ARMOR_INDEX ? sources.get(SOURCE_LEG_ARMOR_INDEX)
					: (i < RESULT_LEG_ARMOR_INDEX ? Collections.emptyList() : resultList.get(RESULT_LEG_ARMOR_INDEX)));
			combinator.set(RESULT_RING1_INDEX, i == RESULT_RING1_INDEX ? sources.get(SOURCE_RING_INDEX)
					: (i < RESULT_RING1_INDEX ? Collections.emptyList() : resultList.get(RESULT_RING1_INDEX)));
			combinator.set(RESULT_RING2_INDEX, i == RESULT_RING2_INDEX ? sources.get(SOURCE_RING_INDEX)
					: (i < RESULT_RING2_INDEX ? Collections.emptyList() : resultList.get(RESULT_RING2_INDEX)));
			combinator.set(RESULT_BOOTS_INDEX, i == RESULT_BOOTS_INDEX ? sources.get(SOURCE_BOOTS_INDEX)
					: (i < RESULT_BOOTS_INDEX ? Collections.emptyList() : resultList.get(RESULT_BOOTS_INDEX)));
			combinator.set(RESULT_SHIELD_INDEX, i == RESULT_SHIELD_INDEX ? sources.get(SOURCE_SHIELD_INDEX)
					: (i < RESULT_SHIELD_INDEX ? Collections.emptyList() : resultList.get(RESULT_SHIELD_INDEX)));
			combinator.set(RESULT_ARTIFACT1_INDEX, i == RESULT_ARTIFACT1_INDEX ? sources.get(SOURCE_ARTIFACT_INDEX)
					: (i < RESULT_ARTIFACT1_INDEX ? Collections.emptyList() : resultList.get(RESULT_ARTIFACT1_INDEX)));
			combinator.set(RESULT_ARTIFACT2_INDEX, i == RESULT_ARTIFACT2_INDEX ? sources.get(SOURCE_ARTIFACT_INDEX)
					: (i < RESULT_ARTIFACT2_INDEX ? Collections.emptyList() : resultList.get(RESULT_ARTIFACT2_INDEX)));
			combinator.set(RESULT_ARTIFACT3_INDEX, i == RESULT_ARTIFACT3_INDEX ? sources.get(SOURCE_ARTIFACT_INDEX)
					: (i < RESULT_ARTIFACT3_INDEX ? Collections.emptyList() : resultList.get(RESULT_ARTIFACT3_INDEX)));
			combinator.set(RESULT_UTILITY1_INDEX, i == RESULT_UTILITY1_INDEX ? sources.get(SOURCE_UTILITY_INDEX)
					: (i < RESULT_UTILITY1_INDEX ? Collections.emptyList() : resultList.get(RESULT_UTILITY1_INDEX)));
			combinator.set(RESULT_UTILITY2_INDEX, i == RESULT_UTILITY2_INDEX ? sources.get(SOURCE_UTILITY_INDEX)
					: (i < RESULT_UTILITY2_INDEX ? Collections.emptyList() : resultList.get(RESULT_UTILITY2_INDEX)));

			if (!FightService.isCombinatoricsTooHigh(maxCombinatoricBeforeActivateReduction, combinator)) {
				continue;
			}
			combinatoricsFightDetailsEvaluator.setExtraCondition(
					() -> FightService.isCombinatoricsTooHigh(maxCombinatoricToActivateReduction, combinator));
			maxEvaluate = i;
			FightDetails maxFightDetails = FightCalculator.DEFAULT_FIGHT_DETAILS;
			effectsCumulator.reset();
			Set<BotItemInfo> itemsSetTemp;
			for (BotItemInfo[] botItemInfos : combinator) {
				if (FightService.validCombinaison(botItemInfos, RESULT_RING1_INDEX, RESULT_RING2_INDEX,
						RESULT_ARTIFACT1_INDEX, RESULT_ARTIFACT2_INDEX, RESULT_ARTIFACT3_INDEX, RESULT_UTILITY1_INDEX,
						RESULT_UTILITY2_INDEX)) {
					for (BotItemInfo botItemInfo : botItemInfos) {
						if (botItemInfo != null) {
							effectService.updateEffectsCumulator(effectsCumulator, botItemInfo.botItemDetails(),
									botItemInfo.quantity());
						}
					}

					// Evaluation
					FightDetails currentFightDetails = fightCalculator.calculateFightResult(monster,
							characterHpWithoutEqt, maxFightDetails.characterTurn(), effectsCumulator);
					// Hypothèse aucun équipement ne fait de récupération de PV
					if (combinatoricsFightDetailsEvaluator.isUpper(maxFightDetails, currentFightDetails)) {
						maxFightDetails = currentFightDetails;
						for (int j = 0; j <= i; j++) {
							itemsSetTemp = tempList.get(j);
							itemsSetTemp.clear();
							if (botItemInfos[j] != null) {
								itemsSetTemp.add(botItemInfos[j]);
							}
						}
						if (maxFightDetails.characterTurn() == 1 && botItemInfos[RESULT_UTILITY1_INDEX] == null
								&& botItemInfos[RESULT_UTILITY2_INDEX] == null) {
							// On a trouvé 1 solution idéale, on arrête la recherche
							break;
						}
					} else if (combinatoricsFightDetailsEvaluator.isEquals(maxFightDetails, currentFightDetails)) {
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
			Set<BotItemInfo> itemsSetTemp = sources.get(j);
			itemsSetTemp.clear();
			switch (j) {
			case SOURCE_RING_INDEX -> {
				itemsSetTemp.addAll(resultList.get(RESULT_RING1_INDEX));
				itemsSetTemp.addAll(resultList.get(RESULT_RING2_INDEX));
			}
			case SOURCE_BOOTS_INDEX -> itemsSetTemp.addAll(resultList.get(RESULT_BOOTS_INDEX));
			case SOURCE_SHIELD_INDEX -> itemsSetTemp.addAll(resultList.get(RESULT_SHIELD_INDEX));
			case SOURCE_ARTIFACT_INDEX -> {
				itemsSetTemp.addAll(resultList.get(RESULT_ARTIFACT1_INDEX));
				itemsSetTemp.addAll(resultList.get(RESULT_ARTIFACT2_INDEX));
				itemsSetTemp.addAll(resultList.get(RESULT_ARTIFACT3_INDEX));
			}
			case SOURCE_UTILITY_INDEX -> {
				itemsSetTemp.addAll(resultList.get(RESULT_UTILITY1_INDEX));
				itemsSetTemp.addAll(resultList.get(RESULT_UTILITY2_INDEX));
				itemsSetTemp.addAll(restoreUtilities);
			}
			case SOURCE_WEAPON_INDEX -> itemsSetTemp.addAll(resultList.get(RESULT_WEAPON_INDEX));
			case SOURCE_BODY_ARMOR_INDEX -> itemsSetTemp.addAll(resultList.get(RESULT_BODY_ARMOR_INDEX));
			case SOURCE_HELMET_INDEX -> itemsSetTemp.addAll(resultList.get(RESULT_HELMET_INDEX));
			case SOURCE_LEG_ARMOR_INDEX -> itemsSetTemp.addAll(resultList.get(RESULT_LEG_ARMOR_INDEX));
			case SOURCE_AMULET_INDEX -> itemsSetTemp.addAll(resultList.get(RESULT_AMULET_INDEX));
			default -> throw new IllegalArgumentException("Value  " + j + " not authorize");
			}
		}
	}
}
