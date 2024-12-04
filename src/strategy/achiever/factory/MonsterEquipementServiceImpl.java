package strategy.achiever.factory;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotMonster;
import strategy.util.BotItemInfo;
import strategy.util.fight.FightService;

public class MonsterEquipementServiceImpl implements MonsterEquipementService {
	private final FightService fightService;
	private String lastMonsterCode;
	private boolean equipmentInProgress;

	public MonsterEquipementServiceImpl(FightService fightService) {
		this.fightService = fightService;
		this.lastMonsterCode = "";
		this.equipmentInProgress = false;
	}

	@Override
	public boolean equipBestEquipement(BotMonster monster, Map<String, Integer> reservedItems) {
		BotItemInfo[] bestEqt = lastMonsterCode.equals(monster.getCode())
				? fightService.optimizeEquipementsInInventory(monster, reservedItems).bestEqt()
				: fightService.optimizeEquipementsPossesed(monster, reservedItems).bestEqt();
		lastMonsterCode = monster.getCode();
		equipmentInProgress = true;
		try {
			return fightService.equipEquipements(bestEqt);
		} finally {
			equipmentInProgress = false;
		}
	}

	@Override
	public void reset() {
		this.lastMonsterCode = "";
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("lastMonsterCode", lastMonsterCode);
		return builder.toString();
	}

	@Override
	public void actionPerformed(String event) {
		if (!equipmentInProgress) {
			reset();
		}
	}
}
