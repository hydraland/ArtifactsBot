package strategy.util.fight;

import java.util.List;
import java.util.Map;

import hydra.model.BotItemType;
import hydra.model.BotMonster;
import strategy.util.BotItemInfo;
import strategy.util.OptimizeResult;

public interface FightService {

	OptimizeResult optimizeEquipementsInInventory(BotMonster monster, Map<String, Integer> reservedItems);

	OptimizeResult optimizeEquipementsPossesed(BotMonster monster, Map<String, Integer> reservedItems);

	Map<String, OptimizeResult> optimizeEquipementsPossesed(List<BotMonster> monsters,
			Map<String, Integer> reservedItems);

	FightDetails calculateFightResult(BotMonster monster);

	OptimizeResult optimizeEquipements(BotMonster monster,
			Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement, int characterHpWithoutEqt);
}