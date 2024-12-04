package strategy;

import hydra.dao.simulate.StopSimulationException;

public final class SumAccumulator {
	private int sum;
	private int max;

	public SumAccumulator() {
		sum = 0;
		max = Integer.MAX_VALUE;
	}

	public void accumulate(int value) {
		sum += value;
		if(sum > max) {
			throw new StopSimulationException();
		}
	}

	public int get() {
		return sum;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public void reset() {
		sum = 0;
	}
}