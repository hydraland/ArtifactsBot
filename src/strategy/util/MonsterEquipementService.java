package strategy.util;

import java.util.Map;

import hydra.model.BotMonster;

public interface MonsterEquipementService {

	boolean equipBestEquipement(BotMonster monster, Map<String, Integer> reservedItems, boolean useUtilities);
}