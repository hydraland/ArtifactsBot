package strategy.achiever.factory.util;

public interface StopChecker<R> {
	boolean isStop(R response);
}
