package strategy.achiever.factory;

import java.util.Collection;
import java.util.List;

import hydra.dao.response.FightResponse;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.goals.MonsterItemDropGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.StopChecker;

public interface GoalFactory {

	List<MonsterGoalAchiever> createMonstersGoals(StopChecker<FightResponse> stopCondition, GoalFilter filter);

	Collection<GoalAchieverInfo<ArtifactGoalAchiever>> createItemsGoals(ChooseBehaviorSelector chooseBehaviorSelector, GoalFilter filter);

	List<GoalAchiever> createManagedInventoryCustomGoal();

	List<GoalAchieverInfo<MonsterItemDropGoalAchiever>> createDropItemGoal();

	ArtifactGoalAchiever addItemRecycleGoalAchiever(GoalAchieverInfo<ArtifactGoalAchiever> goalAchiever, int minPreserve);

	GoalAchiever addDepositNoReservedItemGoalAchiever(GoalAchiever goalAchiever);

	List<GoalAchiever> createTaskGoals();

	public enum GoalFilter {
		EVENT, NO_EVENT, ALL
	}

	GoalAchiever addUsefullGoalToEventGoal(GoalAchiever goalAchiever);
}