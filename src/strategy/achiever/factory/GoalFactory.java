package strategy.achiever.factory;

import java.util.List;

import hydra.dao.response.FightResponse;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.util.StopValidator;
import strategy.achiever.factory.util.GoalAverageOptimizer;

public interface GoalFactory {

	List<MonsterGoalAchiever> createMonstersGoals(StopValidator<FightResponse> stopCondition);

	List<GoalAchiever> createTaskGoals(StopValidator<FightResponse> stopCondition);

	List<ArtifactGoalAchiever> createItemsGoals(ChooseBehaviorSelector chooseBehaviorSelector);

	List<GoalAchiever> createManagedInventoryCustomGoal();

	List<ArtifactGoalAchiever> getDropItemGoal();

	GoalAchieverInfo getInfos(ArtifactGoalAchiever goal);
	
	GoalAverageOptimizer getGoalAverageOptimizer();
	
	GoalAchiever addItemRecycleGoalAchiever(ArtifactGoalAchiever goalAchiever, int minPreserve);

	GoalAchiever addDepositNoReservedItemGoalAchiever(GoalAchiever goalAchiever);

}