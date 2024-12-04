package hydra.dao.simulate;

public interface SimulatorListener {
	void call(String className, String methodName, int cooldown, boolean error);
}
