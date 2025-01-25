package strategy.achiever.factory.goals;

import java.util.List;
import java.util.Map;

import hydra.dao.CharacterDAO;
import hydra.dao.MapDAO;
import hydra.dao.response.FightResponse;
import hydra.model.BotCharacter;
import hydra.model.BotMonster;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.StopChecker;
import strategy.util.MonsterEquipementService;
import strategy.util.MoveService;
import strategy.util.fight.FightService;

public final class MonsterEventGoalAchiever extends MonsterGoalAchiever {

	public MonsterEventGoalAchiever(CharacterDAO characterDAO, MapDAO mapDao, List<Coordinate> coordinates,
			BotMonster monster, MonsterEquipementService monsterEquipementService,
			StopChecker<FightResponse> stopCondition, FightService fightService, MoveService moveService,
			GoalParameter goalParameter) {
		super(characterDAO, mapDao, coordinates, monster, monsterEquipementService, stopCondition, fightService,
				moveService, goalParameter);
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return super.isRealisable(character) && getCoordinates() != null;
	}

	private List<Coordinate> getCoordinates() {
		return this.coordinates != null ? this.coordinates
				: ResourceGoalAchiever.searchCoordinates(mapDao, getMonsterCode(), true);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		if (coordinates == null) {
			coordinates = ResourceGoalAchiever.searchCoordinates(mapDao, getMonsterCode(), true);
			if (coordinates == null) {
				return false;// le monstre n'est plus présent.
			}
		}
		boolean result = super.execute(reservedItems);
		if (!result) {
			this.coordinates = null;
		}
		return result;
	}
	
	@Override
	protected boolean isEventMonster() {
		return true;
	}
}
