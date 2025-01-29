package strategy.achiever.factory.util;

import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;

public final class GoalAverageOptimizerImpl implements GoalAverageOptimizer {
	
	private final CharacterDAO characterDAO;

	public GoalAverageOptimizerImpl(CharacterDAO characterDAO) {
		this.characterDAO = characterDAO;
	}

	@Override
	public int optimize(ArtifactGoalAchiever goalAchiever, int max, float inventoryPercentMaxUse) {
		BotCharacter character = characterDAO.getCharacter();
		int index = 2;
		while (index <= max && goalAchiever.acceptAndSetMultiplierCoefficient(index, new Cumulator(0),
				Math.round(character.getInventoryMaxItems()*inventoryPercentMaxUse))) {
			index++;
		}
		if (index == 2) {
			goalAchiever.acceptAndSetMultiplierCoefficient();
			return 1;
		} else {
			goalAchiever.acceptAndSetMultiplierCoefficient(index - 1, new Cumulator(0),
					Math.round(character.getInventoryMaxItems()*inventoryPercentMaxUse));
			return index - 1;
		}
	}
}
