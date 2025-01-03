package strategy.achiever.factory.goals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.CharacterDAO;
import hydra.dao.MapDAO;
import hydra.dao.response.FightResponse;
import hydra.model.BotCharacter;
import hydra.model.BotMonster;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.StopChecker;
import strategy.util.MonsterEquipementService;
import strategy.util.MoveService;
import strategy.util.fight.FightService;

public class MonsterGoalAchiever implements GoalAchiever {

	private static final HashMap<String, Integer> EMPTY_RESERVED_ITEMS = new HashMap<>();
	protected List<Coordinate> coordinates;
	private final CharacterDAO characterDAO;
	private final BotMonster monster;
	private boolean finish;
	private final MonsterEquipementService monsterEquipementService;
	protected final MapDAO mapDao;
	private final StopChecker<FightResponse> stopCondition;
	private final FightService fightService;
	private final MoveService moveService;
	private final GoalParameter goalParameter;

	public MonsterGoalAchiever(CharacterDAO characterDAO, MapDAO mapDao, List<Coordinate> coordinates,
			BotMonster monster, MonsterEquipementService monsterEquipementService,
			StopChecker<FightResponse> stopCondition, FightService fightService, MoveService moveService, GoalParameter goalParameter) {
		this.mapDao = mapDao;
		this.coordinates = coordinates;
		this.monsterEquipementService = monsterEquipementService;
		this.monster = monster;
		this.characterDAO = characterDAO;
		this.stopCondition = stopCondition;
		this.fightService = fightService;
		this.moveService = moveService;
		this.goalParameter = goalParameter;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return fightService.optimizeEquipementsPossesed(monster, EMPTY_RESERVED_ITEMS).fightDetails().eval() >= 1;
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			if (!goalParameter.getHPRecoveryFactory().createHPRecovery().restoreHP(reservedItems)) {
				return false;
			}
			if (!monsterEquipementService.equipBestEquipement(monster, reservedItems)) {
				return false;
			}
			if (moveService.moveTo(coordinates)) {
				FightResponse response = characterDAO.fight();
				if (response.ok()) {
					return goalParameter.getHPRecoveryFactory().createHPRecovery().restoreHP(reservedItems) && !stopCondition.isStop(response);
				}
				goalParameter.getHPRecoveryFactory().createHPRecovery().restoreHP(reservedItems);
			}
			return false;
		} finally {
			this.finish = true;
		}
	}

	@Override
	public boolean isFinish() {
		return this.finish;
	}

	@Override
	public void clear() {
		this.finish = false;
	}

	@Override
	public void setRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	@Override
	public void unsetRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	public int getMonsterLevel() {
		return monster.getLevel();
	}

	public String getMonsterCode() {
		return monster.getCode();
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("monster", monster);
		return builder.toString();
	}
}
