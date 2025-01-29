package hydra.dao.util;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CooldownManagerImpl implements CooldownManager {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private int cooldownValue;
	private long timestamp;
	
	public CooldownManagerImpl() {
		cooldownValue = 0;
		timestamp = System.currentTimeMillis();
	}

	@Override
	public void begin(int value) {
		this.cooldownValue = value * 1000;
		this.timestamp = System.currentTimeMillis();
	}

	@Override
	public void waitBeforeNextAction() {
		long currentTimestamp = System.currentTimeMillis();
		long stayCoolddown = this.cooldownValue - (currentTimestamp - this.timestamp);
		if(stayCoolddown > 0) {
			sleep(stayCoolddown);
		}
	}
	
	private void sleep(long sleepValue) {
		try {
			Thread.sleep(Duration.ofMillis(sleepValue));
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "CooldownManager sleep error", e);
		}
	}
}
