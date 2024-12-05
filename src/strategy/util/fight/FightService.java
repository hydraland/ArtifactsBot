package strategy.util.fight;

import java.util.List;
import java.util.Map;

import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotMonster;
import strategy.util.BotItemInfo;
import strategy.util.OptimizeResult;

public interface FightService {

	OptimizeResult optimizeEquipementsInInventory(BotMonster monster, Map<String, Integer> reservedItems);

	boolean equipEquipements(BotItemInfo[] bestEqt);

	OptimizeResult optimizeEquipements(BotMonster monster,
			Map<BotCharacterInventorySlot, List<BotItemInfo>> equipableCharacterEquipement);

	OptimizeResult optimizeEquipementsPossesed(BotMonster monster, Map<String, Integer> reservedItems);

	Map<String, OptimizeResult> optimizeEquipementsPossesed(List<BotMonster> monsters, Map<String, Integer> reservedItems);

	FightDetails calculateFightResult(BotMonster monster);
}