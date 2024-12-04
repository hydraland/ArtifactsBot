package strategy.achiever.factory.util;

public interface StopValidator<R> {
	boolean isStop(R response);
}
