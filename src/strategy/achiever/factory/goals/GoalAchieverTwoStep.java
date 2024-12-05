package strategy.achiever.factory.goals;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import strategy.achiever.CheckRealisableGoalAchiever;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.util.Cumulator;

public class GoalAchieverTwoStep implements ArtifactGoalAchiever {

	private boolean finish;
	private final GoalAchiever optionalSubGoal;
	private final GoalAchiever subGoal;
	private final boolean virtualRoot;
	private final boolean checkBeforeExecuteOptional;
	private final CharacterDAO characterDAO;

	public GoalAchieverTwoStep(CharacterDAO characterDAO, GoalAchiever optionalGoal, GoalAchiever goal,
			boolean virtualRoot, boolean checkBeforeExecuteOptional) {
		this.characterDAO = characterDAO;
		this.optionalSubGoal = optionalGoal;
		this.subGoal = goal;
		this.virtualRoot = virtualRoot;
		this.checkBeforeExecuteOptional = checkBeforeExecuteOptional;
		finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		optionalSubGoal.isRealisable(character);
		return subGoal.isRealisable(character);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			if (!checkBeforeExecuteOptional || optionalSubGoal.isRealisable(characterDAO.getCharacter())) {
				optionalSubGoal.execute(reservedItems);
			}
			return (subGoal.execute(reservedItems));
		} finally {
			finish = subGoal.isFinish();
		}
	}

	@Override
	public boolean isFinish() {
		return finish;
	}

	@Override
	public void clear() {
		finish = false;
		subGoal.clear();
		optionalSubGoal.clear();
	}

	@Override
	public void setRoot() {
		if (virtualRoot) {
			optionalSubGoal.setRoot();
			subGoal.setRoot();
		} else {
			optionalSubGoal.unsetRoot();
			subGoal.unsetRoot();
		}
	}

	@Override
	public void unsetRoot() {
		optionalSubGoal.unsetRoot();
		subGoal.unsetRoot();
	}

	@Override
	public double getRate() {
		return (optionalSubGoal instanceof ArtifactGoalAchiever osg ? osg.getRate() : 1)
				* (subGoal instanceof ArtifactGoalAchiever sg ? sg.getRate() : 1);
	}

	final CheckRealisableGoalAchiever getOptionalSubGoal() {
		return optionalSubGoal;
	}

	final CheckRealisableGoalAchiever getSubGoal() {
		return subGoal;
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		if (optionalSubGoal instanceof ArtifactGoalAchiever osg && subGoal instanceof ArtifactGoalAchiever sg) {
			Cumulator optionalCumulator = new Cumulator(cumulator.getValue());
			Cumulator subCumulator = new Cumulator(cumulator.getValue());
			boolean result = osg.acceptAndSetMultiplierCoefficient(coefficient, optionalCumulator, maxItem)
					&& sg.acceptAndSetMultiplierCoefficient(coefficient, subCumulator, maxItem);

			int cumulValue = optionalCumulator.getDiffValue() + subCumulator.getDiffValue() + cumulator.getValue();
			if (result && cumulValue <= maxItem) {
				cumulator.addValue(optionalCumulator.getDiffValue() + subCumulator.getDiffValue());
				return true;
			} else {
				osg.acceptAndSetMultiplierCoefficient();
				return false;
			}
		} else if (subGoal instanceof ArtifactGoalAchiever sg) {
			Cumulator subCumulator = new Cumulator(cumulator.getValue());
			boolean result = sg.acceptAndSetMultiplierCoefficient(coefficient, subCumulator, maxItem);
			cumulator.addValue(subCumulator.getDiffValue());
			return result && cumulator.getValue() <= maxItem;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("virtualRoot", virtualRoot);
		builder.append("checkBeforeExecuteOptional", checkBeforeExecuteOptional);
		builder.append("optionalSubGoal", optionalSubGoal);
		builder.append("subGoal", subGoal);
		return builder.toString();
	}
}
