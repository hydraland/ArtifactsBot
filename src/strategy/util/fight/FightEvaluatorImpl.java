package strategy.util.fight;

import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotMonster;
import strategy.util.BotItemInfo;
import strategy.util.OptimizeResult;
import util.Combinator;

public final class FightEvaluatorImpl implements FightEvaluator {
	private final EffectCumulator effectsCumulator;
	private final EffectService effectService;
	private BotItemInfo[] bestEquipements;
	private FightDetails maxFightDetails;
	private final TurnRestoreLossHPFightDetailsEvaluator fightDetailsEvaluator;
	private final FightCalculator fightCalculator;
	private final ItemDAO itemDAO;
	private BotMonster monster;
	private int characterHpWithoutEqt;

	public FightEvaluatorImpl(EffectService effectService, ItemDAO itemDAO) {
		this.effectService = effectService;
		this.itemDAO = itemDAO;
		this.effectsCumulator = new EffectCumulatorImpl();
		this.fightDetailsEvaluator = new TurnRestoreLossHPFightDetailsEvaluator();
		this.fightCalculator = new FightCalculatorOptimizeTurn();
	}

	@Override
	public OptimizeResult evaluate(Combinator<BotItemInfo> combinator) {
		effectsCumulator.reset();
		for (BotItemInfo[] botItemInfos : combinator) {
			if (FightService.validCombinaison(botItemInfos, OptimizeResult.RING1_INDEX, OptimizeResult.RING2_INDEX,
					OptimizeResult.UTILITY1_INDEX, OptimizeResult.UTILITY2_INDEX, OptimizeResult.ARTIFACT1_INDEX,
					OptimizeResult.ARTIFACT2_INDEX, OptimizeResult.ARTIFACT3_INDEX)) {
				for (BotItemInfo botItemInfo : botItemInfos) {
					if (botItemInfo != null) {
						effectService.updateEffectsCumulator(effectsCumulator, botItemInfo.botItemDetails(),
								botItemInfo.quantity());
					}
				}

				// Evaluation
				FightDetails currentFightDetails = fightCalculator.calculateFightResult(monster, characterHpWithoutEqt,
						maxFightDetails.characterTurn(), effectsCumulator);

				if (fightDetailsEvaluator.isUpper(maxFightDetails, currentFightDetails)) {
					maxFightDetails = currentFightDetails;
					bestEquipements = botItemInfos.clone();
					if (maxFightDetails.characterTurn() == 1 && bestEquipements[OptimizeResult.UTILITY1_INDEX] == null
							&& bestEquipements[OptimizeResult.UTILITY2_INDEX] == null) {
						// On a trouvé 1 solution idéale, on arrête la recherche
						break;
					}
				}
				effectsCumulator.reset();
			}
		}
		return new OptimizeResult(maxFightDetails, bestEquipements);
	}

	@Override
	public void init(BotCharacter character, BotMonster monster, int characterHpWithoutEqt, boolean useUtilities) {
		this.monster = monster;
		this.characterHpWithoutEqt = characterHpWithoutEqt;
		this.bestEquipements = initEquipments(character, useUtilities);
		this.maxFightDetails = calculateFightDetailWithEquipedItems(character, useUtilities);
		;
	}

	@Override
	public OptimizeResult evaluate() {
		return new OptimizeResult(maxFightDetails, bestEquipements);
	}

	private BotItemInfo[] initEquipments(BotCharacter character, boolean useUtilities) {
		return new BotItemInfo[] {
				useUtilities ? initEquipement(character.getUtility1Slot(), character.getUtility1SlotQuantity()) : null,
				useUtilities ? initEquipement(character.getUtility2Slot(), character.getUtility2SlotQuantity()) : null,
				initEquipement(character.getWeaponSlot(), 1), initEquipement(character.getBodyArmorSlot(), 1),
				initEquipement(character.getBootsSlot(), 1), initEquipement(character.getHelmetSlot(), 1),
				initEquipement(character.getShieldSlot(), 1), initEquipement(character.getLegArmorSlot(), 1),
				initEquipement(character.getAmuletSlot(), 1), initEquipement(character.getRing1Slot(), 1),
				initEquipement(character.getRing2Slot(), 1),

				initEquipement(character.getArtifact1Slot(), 1), initEquipement(character.getArtifact2Slot(), 1),
				initEquipement(character.getArtifact3Slot(), 1) };
	}

	private BotItemInfo initEquipement(String slot, int quantity) {
		return "".equals(slot) ? null : new BotItemInfo(itemDAO.getItem(slot), quantity);
	}

	private FightDetails calculateFightDetailWithEquipedItems(BotCharacter character, boolean useUtilities) {
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

		return fightCalculator.calculateFightResult(monster, characterHpWithoutEqt, effectsCumulator);
	}

	private void updateEffectInMapForEquipedEqt(String slot, int quantity) {
		if (!"".equals(slot)) {
			effectService.updateEffectsCumulator(effectsCumulator, itemDAO.getItem(slot), quantity);
		}
	}
}
