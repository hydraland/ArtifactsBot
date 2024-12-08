package hydra.dao;

import java.util.Collections;
import java.util.List;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.ResourcesApi;
import org.openapitools.client.model.DataPageResourceSchema;
import org.openapitools.client.model.ResourceResponseSchema;

import hydra.dao.util.Convertor;
import hydra.model.BotResource;
import util.CacheManager;
import util.PermanentCacheManager;

public class ResourceDAOImpl extends AbstractDAO implements ResourceDAO {
	private static final String RESOURCE_CACHE_NAME = "resource";
	private CacheManager<String, List<BotResource>> cacheManager;
	private final ResourcesApi resourcesApi;

	public ResourceDAOImpl(ApiClient apiClient) {
		resourcesApi = new ResourcesApi(apiClient);
		this.cacheManager = new PermanentCacheManager<>();
	}

	@Override
	public List<BotResource> getAllResources() {
		if (cacheManager.contains(RESOURCE_CACHE_NAME)) {
			return cacheManager.get(RESOURCE_CACHE_NAME);
		} else {
			try {
				ApiResponse<DataPageResourceSchema> response = resourcesApi
						.getAllResourcesResourcesGetWithHttpInfo(null, null, null, null, 1, 100);
				if (isOk(response)) {
					List<BotResource> resources = response.getData().getData().stream()
							.map(res -> Convertor.convert(BotResource.class, res)).toList();
					cacheManager.add(RESOURCE_CACHE_NAME, resources);
					return resources;
				} else {
					return Collections.emptyList();
				}
			} catch (ApiException e) {
				logError(e);
				return Collections.emptyList();
			}
		}
	}

	@Override
	public BotResource getResource(String code) {
		if (cacheManager.contains(RESOURCE_CACHE_NAME)) {
			return cacheManager.get(RESOURCE_CACHE_NAME).stream().filter(br -> br.getCode().equals(code)).findFirst()
					.get();
		} else {
			try {
				ApiResponse<ResourceResponseSchema> response = resourcesApi
						.getResourceResourcesCodeGetWithHttpInfo(code);
				if (isOk(response)) {
					return Convertor.convert(BotResource.class, response.getData().getData());
				} else {
					return null;
				}
			} catch (ApiException e) {
				logError(e);
				return null;
			}
		}
	}
}
