package strategy.achiever.factory.goals;

import java.util.List;
import java.util.Map;

import hydra.dao.CharacterDAO;
import hydra.dao.MapDAO;
import hydra.model.BotCharacter;
import hydra.model.BotResourceSkill;
import strategy.achiever.factory.util.Coordinate;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class GatheringEventGoalAchiever extends GatheringGoalAchiever {

	public GatheringEventGoalAchiever(CharacterDAO characterDAO, CharacterService characterService, MapDAO mapDao,
			String resourceCode, int rate, List<Coordinate> coordinates, int level, BotResourceSkill skill,
			String boxCode, MoveService moveService) {
		super(characterDAO, characterService, mapDao, resourceCode, rate, coordinates, level, skill, boxCode,
				moveService);
	}
	@Override
	public boolean isRealisable(BotCharacter character) {
		return super.isRealisable(character) && getCoordinates() != null;
	}

	private List<Coordinate> getCoordinates() {
		return this.coordinates != null ? this.coordinates
				: ResourceGoalAchiever.searchCoordinates(mapDao, boxCode, false);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		if (coordinates == null) {
			coordinates = ResourceGoalAchiever.searchCoordinates(mapDao, boxCode, false);
			if (coordinates == null) {
				return false;// la ressource n'est plus présente.
			}
		}
		boolean result = super.execute(reservedItems);
		if (!result) {
			this.coordinates = null;
		}
		return result;
	}
}
