package strategy.achiever;

import java.util.concurrent.atomic.AtomicBoolean;

public class GoalInterruptor implements Interruptor {

	private final AtomicBoolean interrupt;
	
	public GoalInterruptor() {
		interrupt = new AtomicBoolean();
	}
	
	@Override
	public void interrupt() {
		interrupt.set(true);
	}

	@Override
	public boolean isInterrupted() {
		return interrupt.get();
	}

	@Override
	public void reset() {
		interrupt.set(false);
	}
}
