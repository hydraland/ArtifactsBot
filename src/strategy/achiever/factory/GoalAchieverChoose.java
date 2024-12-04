package strategy.achiever.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.util.Cumulator;
import strategy.util.CharacterService;

public final class GoalAchieverChoose implements ArtifactGoalAchiever {
	private final Map<ArtifactGoalAchiever, GoalAchieverInfo> goalInfos;
	private final List<ArtifactGoalAchiever> goalAchievers;
	private ArtifactGoalAchiever chooseGoalAchievers;
	private boolean finish;
	private boolean root;
	private final CharacterService characterService;
	private final ChooseBehaviorSelector chooseBehaviorSelector;

	public GoalAchieverChoose(CharacterService characterService, ChooseBehaviorSelector chooseBehaviorSelector) {
		this.goalAchievers = new ArrayList<>();
		this.goalInfos = new HashMap<>();
		this.chooseBehaviorSelector = chooseBehaviorSelector;
		this.characterService = characterService;
		chooseGoalAchievers = null;
		finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		if (root) {
			// search get goal le plus proche du level
			Optional<ArtifactGoalAchiever> searchGoal = goalAchievers.stream().filter(
					aga -> chooseBehaviorSelector.getSelected().accept(getInfo(aga)) && aga.isRealisable(character))
					.min(this::compare);
			if (searchGoal.isPresent()) {
				chooseGoalAchievers = searchGoal.get();
				return true;
			}
		} else {
			for (ArtifactGoalAchiever artifactGoalAchiever : goalAchievers) {
				if (artifactGoalAchiever.isRealisable(character)) {
					chooseGoalAchievers = artifactGoalAchiever;
					return true;
				}
			}
		}
		chooseGoalAchievers = null;
		return false;
	}

	private GoalAchieverInfo getInfo(ArtifactGoalAchiever aga) {
		return goalInfos.get(aga);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			if (chooseGoalAchievers != null) {
				return chooseGoalAchievers.execute(reservedItems);
			}
			return false;
		} finally {
			finish = chooseGoalAchievers == null || chooseGoalAchievers.isFinish();
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

	@Override
	public void setRoot() {
		root = true;
		setRootChildren();
	}

	private void setRootChildren() {
		for (GoalAchiever goalAchiever : goalAchievers) {
			goalAchiever.setRoot();
		}
	}

	@Override
	public void unsetRoot() {
		root = false;
		unsetRootChildren();
	}

	private void unsetRootChildren() {
		for (GoalAchiever goalAchiever : goalAchievers) {
			goalAchiever.unsetRoot();
		}
	}

	@Override
	public double getRate() {
		if (chooseGoalAchievers == null) {
			return goalAchievers.stream().map(aga -> aga.getRate()).reduce(0d, (a, b) -> a + b) / goalAchievers.size();
		} else {
			return chooseGoalAchievers.getRate();
		}
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		int maxDiffValue = 0;
		for (ArtifactGoalAchiever artifactGoalAchiever : goalAchievers) {
			Cumulator subCumulator = new Cumulator(cumulator.getValue());
			boolean result = artifactGoalAchiever.acceptAndSetMultiplierCoefficient(coefficient, subCumulator, maxItem);
			if (!result) {
				goalAchievers.stream().forEach(aga -> aga.acceptAndSetMultiplierCoefficient());
				return false;
			}
			if (subCumulator.getDiffValue() > maxDiffValue) {
				maxDiffValue = subCumulator.getDiffValue();
			}
		}
		cumulator.addValue(maxDiffValue);
		return cumulator.getValue() <= maxItem;
	}

	void addGoal(ArtifactGoalAchiever goalToAdd, GoalAchieverInfo infos) {
		goalAchievers.add(goalToAdd);
		goalAchievers.sort(this::compareRate);
		goalInfos.put(goalToAdd, infos);
	}

	private int compare(ArtifactGoalAchiever artifactgoalachiever1, ArtifactGoalAchiever artifactgoalachiever2) {
		int info1DistanceLevelValue = getDistanceLevel(getInfo(artifactgoalachiever1));
		int info2DistanceLevelValue = getDistanceLevel(getInfo(artifactgoalachiever2));

		return Integer.compare(info1DistanceLevelValue, info2DistanceLevelValue);
	}

	private int getDistanceLevel(GoalAchieverInfo info) {
		int distanceLevel = Integer.MAX_VALUE;
		if (info.isCraft()) {
			int levelCharacter = characterService.getLevel(info.getBotCraftSkill());
			if (info.getLevel() <= levelCharacter) {
				distanceLevel = levelCharacter - info.getLevel();
			}
		}
		if (info.isGathering()) {
			int levelCharacter = characterService.getLevel(info.getBotResourceSkill());
			if (info.getLevel() <= levelCharacter) {
				distanceLevel = levelCharacter - info.getLevel();
			}
		}
		return distanceLevel;
	}

	private int compareRate(ArtifactGoalAchiever artifactgoalachiever1, ArtifactGoalAchiever artifactgoalachiever2) {
		return Double.compare(artifactgoalachiever1.getRate(), artifactgoalachiever2.getRate());
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("root", root);
		builder.append("chooseGoalAchievers", chooseGoalAchievers);
		builder.append("goalAchievers", goalAchievers);
		return builder.toString();
	}

	public interface ChooseBehavior {
		boolean accept(GoalAchieverInfo info);
	}

	public interface ChooseBehaviorSelector {
		static final ChooseBehavior CRAFTING = info -> info.isCraft();
		static final ChooseBehavior GATHERING = info -> info.isGathering();
		static final ChooseBehavior CRAFTING_AND_GATHERING = info -> CRAFTING.accept(info) || GATHERING.accept(info);

		ChooseBehavior getSelected();
	}
}
