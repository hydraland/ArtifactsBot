package hydra.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.MapsApi;
import org.openapitools.client.model.DataPageMapSchema;
import org.openapitools.client.model.MapContentType;

import hydra.dao.util.Convertor;
import hydra.model.BotBox;
import util.CacheManager;
import util.PermanentCacheManager;

public class MapDAOImpl extends AbstractDAO implements MapDAO {
	private final CacheManager<String, List<BotBox>> cacheManager;
	private final MapsApi mapsApi;

	public MapDAOImpl(ApiClient apiClient) {
		mapsApi = new MapsApi(apiClient);
		this.cacheManager = new PermanentCacheManager<>();
	}

	@Override
	public List<BotBox> getResourcesBox() {
		return getBox("resource", false);
	}

	@Override
	public List<BotBox> getMonstersBox() {
		return getBox("monster", false);
	}

	@Override
	public List<BotBox> getTasksBox() {
		return getBox("tasks_master", true);
	}

	@Override
	public List<BotBox> getWorkshopsBox() {
		return getBox("workshop", true);
	}

	@Override
	public List<BotBox> getGrandExchangesBox() {
		return getBox("grand_exchange", true);
	}

	@Override
	public List<BotBox> getBanksBox() {
		return getBox("bank", true);
	}

	@Override
	public List<BotBox> getAllBox() {
		List<BotBox> currentResultList = new ArrayList<>();
		try {
			ApiResponse<DataPageMapSchema> response = mapsApi.getAllMapsMapsGetWithHttpInfo(null, null, 1, 100);
			if (isOk(response)) {
				currentResultList.addAll(response.getData().getData().stream()
						.map(box -> Convertor.convert(BotBox.class, box)).toList());
				for (int i = 2; i <= response.getData().getPages(); i++) {
					response = mapsApi.getAllMapsMapsGetWithHttpInfo(null, null, i, 100);
					if (isOk(response)) {
						currentResultList.addAll(response.getData().getData().stream()
								.map(box -> Convertor.convert(BotBox.class, box)).toList());
					} else {
						return Collections.emptyList();
					}
				}
				return currentResultList;
			} else {
				return Collections.emptyList();
			}
		} catch (ApiException e) {
			logError(e);
			return Collections.emptyList();
		}
	}

	private List<BotBox> getBox(String type, boolean cache) {
		if (cacheManager.contains(type)) {
			return cacheManager.get(type);
		}
		try {
			ApiResponse<DataPageMapSchema> response = mapsApi
					.getAllMapsMapsGetWithHttpInfo(MapContentType.valueOf(type.toUpperCase()), null, 1, 100);
			if (isOk(response)) {
				List<BotBox> result = response.getData().getData().stream()
						.map(box -> Convertor.convert(BotBox.class, box)).toList();
				if (cache) {
					cacheManager.add(type, result);
				}
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
