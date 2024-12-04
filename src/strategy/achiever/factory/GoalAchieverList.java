package strategy.achiever.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.util.Cumulator;

public class GoalAchieverList implements ArtifactGoalAchiever {

	private List<ArtifactGoalAchiever> goalAchievers;
	private boolean finish;

	public GoalAchieverList() {
		goalAchievers = new ArrayList<>();
		finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return goalAchievers.stream().map(collect -> collect.isRealisable(character))
				.collect(Collectors.reducing((val1, val2) -> val1 && val2)).get();
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			for (GoalAchiever goalAchiever : goalAchievers) {
				while (!goalAchiever.isFinish()) {
					if (!goalAchiever.execute(reservedItems)) {
						return false;
					}
				}
			}
			return true;
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
		for (GoalAchiever goalAchiever : goalAchievers) {
			goalAchiever.clear();
		}
	}

	public void add(ArtifactGoalAchiever goalAchiever) {
		goalAchievers.add(goalAchiever);
	}

	@Override
	public void setRoot() {
		unsetRootChildren();
	}

	@Override
	public void unsetRoot() {
		unsetRootChildren();
	}

	private void unsetRootChildren() {
		for (GoalAchiever goalAchiever : goalAchievers) {
			goalAchiever.unsetRoot();
		}
	}

	@Override
	public double getRate() {
		return goalAchievers.stream().map(c -> c.getRate()).reduce(1d, (a, b) -> a * b);
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		Cumulator intermediateCumulator = new Cumulator(cumulator.getValue());
		for (ArtifactGoalAchiever artifactGoalAchiever : goalAchievers) {
			Cumulator subCumulator = new Cumulator(intermediateCumulator.getValue());
			boolean result = artifactGoalAchiever.acceptAndSetMultiplierCoefficient(coefficient, subCumulator, maxItem);
			if (!result) {
				goalAchievers.stream().forEach(aga -> aga.acceptAndSetMultiplierCoefficient());
				return false;
			}
			intermediateCumulator.addValue(subCumulator.getDiffValue());
		}
		cumulator.addValue(intermediateCumulator.getDiffValue());
		return cumulator.getValue() <= maxItem;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("goalAchievers", goalAchievers);
		return builder.toString();
	}
}
