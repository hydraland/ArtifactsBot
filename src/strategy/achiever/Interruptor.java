package strategy.achiever;

public interface Interruptor {
	void interrupt();
	
	boolean isInterrupted();
	
	void reset();
}
