package strategy.achiever.factory.goals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import strategy.achiever.factory.util.Cumulator;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public final class ItemCraftGoalAchiever implements ResourceGoalAchiever {

	private final String code;
	private final CharacterDAO characterDAO;
	private final BotCraftSkill skill;
	private final int level;
	private final ArtifactGoalAchiever subGoal;
	private boolean finish;
	private boolean root;
	private int coefficient;
	private final MoveService moveService;
	private final CharacterService characterService;

	public ItemCraftGoalAchiever(CharacterDAO characterDAO, CharacterService characterService, String code,
			MoveService moveService, int level, BotCraftSkill skill, ArtifactGoalAchiever goalAchiever) {
		this.characterService = characterService;
		this.code = code;
		this.moveService = moveService;
		this.level = level;
		this.skill = skill;
		this.characterDAO = characterDAO;
		this.subGoal = goalAchiever;
		this.finish = false;
		this.coefficient = 1;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		int skillLevel = characterService.getLevel(skill);
		return skillLevel >= this.level && subGoal.isRealisable(character);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		int craftQuantity = coefficient;
		try {
			boolean changeOptim = false;

			if (!root) {
				int reserveInInventory = ResourceGoalAchiever.reserveInInventory(characterService, code, reservedItems,
						craftQuantity);
				if (reserveInInventory == craftQuantity) {
					return true;
				}
				craftQuantity -= reserveInInventory;
				if (coefficient > 1) {
					// S'il est optimisé on reduit le nombre necessaire de ressources
					subGoal.acceptAndSetMultiplierCoefficient(craftQuantity);
					changeOptim = true;
				}
			}

			Map<String, Integer> reservedItemsBefore = new HashMap<>(reservedItems);
			if (subGoal.execute(reservedItemsBefore)) {
				if (changeOptim) {
					// On restaure l'ancienne valeur
					subGoal.acceptAndSetMultiplierCoefficient(coefficient);
				}
				if (moveService.moveTo(skill)) {
					boolean result = characterDAO.craft(code, craftQuantity).ok();
					if (result && !root) {
						ResourceGoalAchiever.reserveItem(code, reservedItems, craftQuantity);
					}
					return result;
				}
			}
			return false;
		} finally {
			this.finish = true;
		}
	}

	@Override
	public boolean isFinish() {
		return finish;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public void clear() {
		finish = false;
		subGoal.clear();
	}

	@Override
	public void setRoot() {
		this.root = true;
		subGoal.unsetRoot();
	}

	@Override
	public void unsetRoot() {
		this.root = false;
		subGoal.unsetRoot();
	}

	public BotCraftSkill getSkill() {
		return skill;
	}

	@Override
	public double getRate() {
		return subGoal.getRate();
	}

	public final int getLevel() {
		return level;
	}

	final ArtifactGoalAchiever getSubGoal() {
		return subGoal;
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		if (coefficient + cumulator.getValue() <= maxItem) {
			Cumulator subCumulator = new Cumulator(cumulator.getValue());
			boolean result = subGoal.acceptAndSetMultiplierCoefficient(coefficient, subCumulator, maxItem);
			if (result && subCumulator.getValue() <= maxItem) {
				this.coefficient = coefficient;
				cumulator.addValue(coefficient);
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("code", code);
		builder.append("level", level);
		builder.append("skill", skill);
		builder.append("root", root);
		builder.append("coefficient", coefficient);
		builder.append("subGoal", subGoal);
		return builder.toString();
	}
}