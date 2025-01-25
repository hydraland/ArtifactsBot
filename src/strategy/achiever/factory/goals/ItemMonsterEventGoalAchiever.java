package strategy.achiever.factory.goals;

import java.util.List;
import java.util.Map;

import hydra.dao.CharacterDAO;
import hydra.dao.MapDAO;
import hydra.model.BotCharacter;
import hydra.model.BotMonster;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.util.Coordinate;
import strategy.util.CharacterService;
import strategy.util.MonsterEquipementService;
import strategy.util.MoveService;
import strategy.util.fight.FightService;

public final class ItemMonsterEventGoalAchiever extends ItemMonsterGoalAchiever {

	public ItemMonsterEventGoalAchiever(CharacterDAO characterDAO, MapDAO mapDao, String resourceCode, int rate,
			List<Coordinate> coordinates, BotMonster monster, MonsterEquipementService monsterEquipementService,
			FightService fightService, MoveService moveService, CharacterService characterService,
			GoalParameter goalParameter) {
		super(characterDAO, mapDao, resourceCode, rate, coordinates, monster, monsterEquipementService, fightService,
				moveService, characterService, goalParameter);
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return super.isRealisable(character) && getCoordinates() != null;
	}
	
	@Override
	protected boolean isEventMonster() {
		return true;
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
}
