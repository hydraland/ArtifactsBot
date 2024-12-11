package strategy;

import hydra.dao.simulate.SimulatorListener;

public final class GenericSimulatorListener implements SimulatorListener {
	private SimulatorListener innerListener;

	public GenericSimulatorListener() {
		innerListener = (className, methodName, cooldown, error) -> {
		};
	}

	@Override
	public void call(String className, String methodName, int cooldown, boolean error) {
		innerListener.call(className, methodName, cooldown, error);
	}

	public final void setInnerListener(SimulatorListener innerListener) {
		this.innerListener = innerListener;
	}
}