package strategy.util;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotMonster;
import strategy.util.fight.FightService;

public class MonsterEquipementServiceImpl implements MonsterEquipementService {
	private final FightService fightService;

	public MonsterEquipementServiceImpl(FightService fightService) {
		this.fightService = fightService;
	}

	@Override
	public boolean equipBestEquipement(BotMonster monster, Map<String, Integer> reservedItems) {
		BotItemInfo[] bestEqt = fightService.optimizeEquipementsPossesed(monster, reservedItems).bestEqt();
		return fightService.equipEquipements(bestEqt);
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
