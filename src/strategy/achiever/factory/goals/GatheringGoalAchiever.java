package strategy.achiever.factory.goals;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.CharacterDAO;
import hydra.dao.MapDAO;
import hydra.dao.response.GatheringResponse;
import hydra.model.BotCharacter;
import hydra.model.BotItemReader;
import hydra.model.BotResourceSkill;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.Cumulator;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class GatheringGoalAchiever implements ResourceGoalAchiever {

	private final String resourceCode;
	private final int rate;
	private List<Coordinate> coordinates;
	private final CharacterDAO characterDAO;
	private final BotResourceSkill skill;
	private final int level;
	private boolean finish;
	private boolean root;
	private final String boxCode;
	private final MapDAO mapDao;
	private final MoveService moveService;
	private final CharacterService characterService;

	public GatheringGoalAchiever(CharacterDAO characterDAO, CharacterService characterService, MapDAO mapDao,
			String resourceCode, int rate, List<Coordinate> coordinates, int level, BotResourceSkill skill,
			String boxCode, MoveService moveService) {
		this.characterService = characterService;
		this.mapDao = mapDao;
		this.resourceCode = resourceCode;
		this.rate = rate;
		this.coordinates = coordinates;
		this.level = level;
		this.skill = skill;
		this.characterDAO = characterDAO;
		this.boxCode = boxCode;
		this.moveService = moveService;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		int skillLevel = characterService.getLevel(skill);
		return skillLevel >= this.level && getCoordinates() != null;
	}

	private List<Coordinate> getCoordinates() {
		return this.coordinates != null ? this.coordinates
				: ResourceGoalAchiever.searchCoordinates(mapDao, boxCode, false);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		if (!root) {
			int nbReserved = ResourceGoalAchiever.reserveInInventory(characterService, getCode(), reservedItems, 1);
			if (nbReserved == 1) {
				this.finish = true;
				return true;
			}
		}
		if (coordinates == null) {
			coordinates = ResourceGoalAchiever.searchCoordinates(mapDao, boxCode, false);
			if (coordinates == null) {
				return false;// la ressource n'est plus présente.
			}
		}
		if (moveService.moveTo(coordinates)) {
			GatheringResponse response = characterDAO.collect();
			if (response.resourceNotFound()) {
				this.coordinates = null;
			}
			if (response.ok()) {
				List<BotItemReader> items = response.botDetails().getItems();
				for (BotItemReader botItem : items) {
					String itemCode = botItem.getCode();
					if (itemCode.equals(this.resourceCode)) {
						ResourceGoalAchiever.reserveItem(itemCode, reservedItems, 1);
						this.finish = true;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isFinish() {
		return this.finish;
	}

	@Override
	public String getCode() {
		return this.resourceCode;
	}

	@Override
	public void clear() {
		this.finish = false;
	}

	@Override
	public void setRoot() {
		this.root = true;
	}

	@Override
	public void unsetRoot() {
		this.root = false;
	}

	@Override
	public double getRate() {
		return (1d / this.rate);
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		cumulator.addValue(1);
		return coefficient == 1 && cumulator.getValue() <= maxItem;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("resourceCode", resourceCode);
		builder.append("level", level);
		builder.append("root", root);
		builder.append("rate", rate);
		builder.append("skill", skill);
		return builder.toString();
	}
}
