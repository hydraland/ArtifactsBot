package strategy.achiever.factory;

import java.util.Collection;
import java.util.List;

import hydra.dao.response.FightResponse;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.StopChecker;

public interface GoalFactory {

	List<MonsterGoalAchiever> createMonstersGoals(StopChecker<FightResponse> stopCondition);

	Collection<GoalAchieverInfo> createItemsGoals(ChooseBehaviorSelector chooseBehaviorSelector);

	List<GoalAchiever> createManagedInventoryCustomGoal();

	List<GoalAchieverInfo> getDropItemGoal();

	GoalAchiever addItemRecycleGoalAchiever(GoalAchieverInfo goalAchiever, int minPreserve);

	GoalAchiever addDepositNoReservedItemGoalAchiever(GoalAchiever goalAchiever);

	List<GoalAchiever> createTaskGoals();

}