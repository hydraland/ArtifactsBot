package strategy.achiever.factory;

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
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.StopValidator;
import strategy.util.MoveService;
import strategy.util.fight.FightService;

public class MonsterGoalAchiever implements GoalAchiever {

	private static final HashMap<String, Integer> EMPTY_RESERVED_ITEMS = new HashMap<>();
	private List<Coordinate> coordinates;
	private final CharacterDAO characterDAO;
	private final BotMonster monster;
	private boolean finish;
	private final MonsterEquipementService monsterEquipementService;
	private final MapDAO mapDao;
	private final StopValidator<FightResponse> stopCondition;
	private final FightService fightService;
	private final MoveService moveService;

	public MonsterGoalAchiever(CharacterDAO characterDAO, MapDAO mapDao, List<Coordinate> coordinates,
			BotMonster monster, MonsterEquipementService monsterEquipementService,
			StopValidator<FightResponse> stopCondition, FightService fightService, MoveService moveService) {
		this.mapDao = mapDao;
		this.coordinates = coordinates;
		this.monsterEquipementService = monsterEquipementService;
		this.monster = monster;
		this.characterDAO = characterDAO;
		this.stopCondition = stopCondition;
		this.fightService = fightService;
		this.moveService = moveService;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		monsterEquipementService.reset();
		return fightService.optimizeEquipementsPossesed(monster, EMPTY_RESERVED_ITEMS).fightDetails().eval() > 1d
				&& getCoordinates() != null;
	}

	private List<Coordinate> getCoordinates() {
		return this.coordinates != null ? this.coordinates
				: ResourceGoalAchiever.searchCoordinates(mapDao, monster.getCode(), true);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			if (!monsterEquipementService.equipBestEquipement(monster, reservedItems)) {
				return false;
			}
			if (coordinates == null) {
				coordinates = ResourceGoalAchiever.searchCoordinates(mapDao, monster.getCode(), true);
				if (coordinates == null) {
					return false;// le monstre n'est plus présent.
				}
			}
			if (!fightService.restoreHP(reservedItems)) {
				return false;
			}
			if (moveService.moveTo(coordinates)) {
				FightResponse response = characterDAO.fight();
				if (response.monsterNotFound()) {
					this.coordinates = null;
				}
				if (response.ok()) {
					if (!fightService.restoreHP(reservedItems)) {
						return false;
					}
					return !stopCondition.isStop(response);
				}
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
