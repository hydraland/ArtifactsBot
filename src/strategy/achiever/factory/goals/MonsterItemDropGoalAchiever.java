package strategy.achiever.factory.goals;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.Cumulator;

public final class MonsterItemDropGoalAchiever implements ArtifactGoalAchiever {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private final GoalParameter parameter;
	private boolean finish;
	private CharacterDAO characterDAO;
	private final GoalAchieverInfo<ArtifactGoalAchiever> dropGoalInfo;

	public MonsterItemDropGoalAchiever(GoalAchieverInfo<ArtifactGoalAchiever> dropGoalInfo, CharacterDAO characterDAO,
			GoalParameter parameter) {
		this.dropGoalInfo = dropGoalInfo;
		this.characterDAO = characterDAO;
		this.parameter = parameter;
		finish = false;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return true;
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			GoalAchiever goalAchiever = parameter.getItemDropFactory().createItemGoalAchiever(dropGoalInfo);
			LOGGER.info("Execute ItemDrop Goal");
			LOGGER.info(goalAchiever.toString());
			if (goalAchiever.isRealisableAfterSetRoot(characterDAO.getCharacter())) {
				LOGGER.info("Goal realisable");
				goalAchiever.clear();
				if (!goalAchiever.execute(reservedItems)) {
					LOGGER.info("Goal fail");
					return false;
				}
				LOGGER.info("Goal succes");
				return true;
			}
			return false;
		} finally {
			finish = true;
		}
	}

	@Override
	public boolean isFinish() {
		return finish;
	}

	@Override
	public void clear() {
		finish = false;
	}

	@Override
	public void setRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	@Override
	public void unsetRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	@Override
	public double getRate() {
		return dropGoalInfo.getGoal().getRate();
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		return dropGoalInfo.getGoal().acceptAndSetMultiplierCoefficient(coefficient, cumulator, maxItem);
	}

	public final ArtifactGoalAchiever getDropGoal() {
		return dropGoalInfo.getGoal();
	}
}