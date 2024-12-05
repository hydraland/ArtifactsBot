package strategy.util;

import java.util.Map;

import hydra.model.BotMonster;
import util.EventListener;

public interface MonsterEquipementService extends EventListener<String> {

	boolean equipBestEquipement(BotMonster monster, Map<String, Integer> reservedItems);

	void reset();
}