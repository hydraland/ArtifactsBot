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
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.Cumulator;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class GatheringGoalAchiever implements ResourceGoalAchiever {

	private final String resourceCode;
	private final int rate;
	protected List<Coordinate> coordinates;
	private final CharacterDAO characterDAO;
	private final BotResourceSkill skill;
	private final int level;
	private boolean finish;
	private boolean root;
	protected final String boxCode;
	protected final MapDAO mapDao;
	private final MoveService moveService;
	private final CharacterService characterService;
	private final GoalAchiever equipToolGoal;
	private boolean equipTool;

	public GatheringGoalAchiever(GoalAchiever equipToolGoal, CharacterDAO characterDAO,
			CharacterService characterService, MapDAO mapDao, String resourceCode, int rate,
			List<Coordinate> coordinates, int level, BotResourceSkill skill, String boxCode, MoveService moveService) {
		this.equipToolGoal = equipToolGoal;
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
		this.equipTool = true;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		int skillLevel = characterService.getLevel(skill);
		return skillLevel >= this.level && equipToolGoal.isRealisable(character);
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

		if (equipTool) {
			equipTool = !equipToolGoal.execute(reservedItems);
		}
		if (moveService.moveTo(coordinates)) {
			GatheringResponse response = characterDAO.collect();
			if (response.ok()) {
				List<? extends BotItemReader> items = response.botDetails().getItems();
				for (BotItemReader botItem : items) {
					String itemCode = botItem.getCode();
					if (itemCode.equals(this.resourceCode)) {
						if (!root) {
							ResourceGoalAchiever.reserveItem(itemCode, reservedItems, 1);
						}
						this.finish = true;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean isFinish() {
		return finish;
	}

	@Override
	public final String getCode() {
		return resourceCode;
	}

	@Override
	public final void clear() {
		finish = false;
		equipTool = true;
		equipToolGoal.clear();
	}

	@Override
	public final void setRoot() {
		root = true;
	}

	@Override
	public final void unsetRoot() {
		root = false;
	}

	@Override
	public final double getRate() {
		return (1d / rate);
	}

	@Override
	public final boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		cumulator.addValue(1);
		return coefficient == 1 && cumulator.getValue() <= maxItem;
	}

	@Override
	public final String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("resourceCode", resourceCode);
		builder.append("level", level);
		builder.append("root", root);
		builder.append("rate", rate);
		builder.append("skill", skill);
		builder.append("equipToolGoal", equipToolGoal);
		return builder.toString();
	}
}
