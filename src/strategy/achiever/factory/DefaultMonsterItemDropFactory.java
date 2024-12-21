package strategy.achiever.factory;

import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;

public class DefaultMonsterItemDropFactory implements MonsterItemDropFactory {

	private final GoalFactoryCreator factoryCreator;
	public DefaultMonsterItemDropFactory(GoalFactoryCreator factoryCreator) {
		this.factoryCreator = factoryCreator;
	}
	
	@Override
	public GoalAchiever createItemGoalAchiever(GoalAchieverInfo<ArtifactGoalAchiever> dropGoalInfo) {
		return factoryCreator.createGoalAchieverLoop(dropGoalInfo.getGoal(), 1, false);
	}
}
