package strategy.achiever.factory;

import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;

public interface MonsterItemDropFactory {
	GoalAchiever createItemGoalAchiever(GoalAchieverInfo<ArtifactGoalAchiever> dropGoalInfo);
}
