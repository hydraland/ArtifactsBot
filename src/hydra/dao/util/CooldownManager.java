package hydra.dao.util;

public interface CooldownManager {

	void begin(int value);

	void waitBeforeNextAction();

}