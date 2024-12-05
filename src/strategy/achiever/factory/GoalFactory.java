package strategy.achiever.factory;

import java.util.List;

import hydra.dao.response.FightResponse;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.StopValidator;

public interface GoalFactory {

	List<MonsterGoalAchiever> createMonstersGoals(StopValidator<FightResponse> stopCondition);

	List<ArtifactGoalAchiever> createItemsGoals(ChooseBehaviorSelector chooseBehaviorSelector);

	List<GoalAchiever> createManagedInventoryCustomGoal();

	List<ArtifactGoalAchiever> getDropItemGoal();

	GoalAchieverInfo getInfos(ArtifactGoalAchiever goal);
	
	GoalAchiever addItemRecycleGoalAchiever(ArtifactGoalAchiever goalAchiever, int minPreserve);

	GoalAchiever addDepositNoReservedItemGoalAchiever(GoalAchiever goalAchiever);

	List<GoalAchiever> createTaskGoals();

}