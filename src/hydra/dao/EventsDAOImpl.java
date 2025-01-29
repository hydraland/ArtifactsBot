package hydra.dao;

import java.util.Collections;
import java.util.List;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.EventsApi;
import org.openapitools.client.model.DataPageActiveEventSchema;
import org.openapitools.client.model.DataPageEventSchema;

import hydra.dao.util.Convertor;
import hydra.model.BotActiveEvent;
import hydra.model.BotEvent;
import util.CacheManager;
import util.PermanentCacheManager;

public final class EventsDAOImpl extends AbstractDAO implements EventsDAO {
	private final CacheManager<String, List<BotEvent>> cacheManager;
	private final EventsApi eventsApi;

	public EventsDAOImpl(ApiClient apiClient) {
		eventsApi = new EventsApi(apiClient);
		this.cacheManager = new PermanentCacheManager<>();
	}

	@Override
	public List<BotActiveEvent> getActiveEvents() {
		try {
			ApiResponse<DataPageActiveEventSchema> response = eventsApi.getAllActiveEventsEventsActiveGetWithHttpInfo(1,
					100);
			if (isOk(response)) {
				return response.getData().getData().stream()
						.<BotActiveEvent>map(event -> Convertor.convert(BotActiveEvent.class, event)).toList();
			} else {
				return Collections.emptyList();
			}
		} catch (ApiException e) {
			logError(e);
			return Collections.emptyList();
		}
	}

	@Override
	public List<BotEvent> getAllEvents() {
		if (cacheManager.contains("all")) {
			return cacheManager.get("all");
		}
		try {
			ApiResponse<DataPageEventSchema> response = eventsApi.getAllEventsEventsGetWithHttpInfo(1, 100);
			if (isOk(response)) {
				List<BotEvent> result = response.getData().getData().stream()
						.<BotEvent>map(event -> Convertor.convert(BotEvent.class, event)).toList();
				cacheManager.add("all", result);
				return result;

			} else {
				return Collections.emptyList();
			}
		} catch (ApiException e) {
			logError(e);
			return Collections.emptyList();
		}
	}
}
