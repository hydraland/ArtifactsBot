package hydra.dao.simulate;

public final class FilteredInnerCallSimulatorListener implements SimulatorListener {

	private final SimulatorListener simulatorListener;
	private boolean innerCall;
	private int callNumber;

	public FilteredInnerCallSimulatorListener(SimulatorListener simulatorListener) {
		this.simulatorListener = simulatorListener;
		innerCall = false;
		callNumber = 0;
	}

	void startInnerCall() {
		this.innerCall = true;
		callNumber++;
	}

	void stopInnerCall() {
		callNumber--;
		if (callNumber == 0) {
			this.innerCall = false;
		}
	}

	@Override
	public void call(String className, String methodName, int cooldown, boolean error) {
		if (!innerCall) {
			simulatorListener.call(className, methodName, cooldown, error);
		}
	}
}
