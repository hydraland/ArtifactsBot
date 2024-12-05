package strategy.achiever.factory;

import strategy.achiever.GoalAchiever;

public interface MonsterTaskFactory {
	GoalAchiever createTaskGoalAchiever(String code, int total);
}
