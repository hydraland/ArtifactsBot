package util;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface Utility {
	static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	public static void sleep(long seconds) {
		try {
			Thread.sleep(Duration.ofSeconds(seconds));
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Utility sleep error", e);
		}
	}
}
