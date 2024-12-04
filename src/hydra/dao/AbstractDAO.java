package hydra.dao;

import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;

public abstract class AbstractDAO {
	private static final int OK_CODE = 200;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	protected final boolean isOk(ApiResponse<?> response) {
		return response.getStatusCode() == OK_CODE;
	}
	
	protected void logError(ApiException piae) {
		LOGGER.log(Level.SEVERE, "API error", piae);
	}
	
	protected void logMessage(String msg) {
		LOGGER.info(msg);
	}
}
