package strategy.achiever.factory;

import java.util.List;

import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.util.Coordinate;

public interface ItemTaskFactory {
	GoalAchiever createTaskGoalAchiever(String code, int total, List<Coordinate> taskMasterCoordinates);
}
