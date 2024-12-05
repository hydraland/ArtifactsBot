package strategy.achiever;

import java.util.Map;

public interface ExecutableGoalAchiever {

	boolean execute(Map<String, Integer> reservedItems);

}