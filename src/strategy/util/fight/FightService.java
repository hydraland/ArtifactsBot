package strategy.util.fight;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hydra.model.BotItemType;
import hydra.model.BotMonster;
import strategy.util.BotItemInfo;
import strategy.util.OptimizeResult;
import util.Combinator;

public interface FightService {

	OptimizeResult optimizeEquipementsInInventory(BotMonster monster, Map<String, Integer> reservedItems,
			boolean useUtilities);

	OptimizeResult optimizeEquipementsPossesed(BotMonster monster, Map<String, Integer> reservedItems,
			boolean useUtilities);

	Map<String, OptimizeResult> optimizeEquipementsPossesed(List<BotMonster> monsters,
			Map<String, Integer> reservedItems);

	FightDetails calculateFightResult(BotMonster monster);

	OptimizeResult optimizeEquipements(BotMonster monster,
			Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement, int characterHpWithoutEqt);

	static boolean isCombinatoricsTooHigh(long maxCombinatoricsValue, Combinator<?> combinator) {
		long currentVal = 1l;
		for (int i = 0; i < combinator.size(); i++) {
			int size = combinator.size(i);
			currentVal *= size == 0 ? 1 : size;
			if (currentVal > maxCombinatoricsValue) {
				return true;
			}
		}
		return false;
	}

	static boolean isCombinatoricsTooHigh(long maxCombinatoricsValue, int... values) {
		long currentVal = 1l;
		for (int value : values) {
			currentVal *= value == 0 ? 1 : value;
			if (currentVal > maxCombinatoricsValue) {
				return true;
			}
		}
		return false;
	}

	static boolean validCombinaison(BotItemInfo[] botItemInfos, int ringIndex1, int ringIndex2,
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

	static void addNullValueIfAbsent(Set<BotItemInfo> botItemList) {
		if (!botItemList.isEmpty() && !botItemList.contains(null)) {
			botItemList.add(null);
		}
	}
}