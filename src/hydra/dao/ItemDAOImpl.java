package hydra.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.ItemsApi;
import org.openapitools.client.model.CraftSkill;
import org.openapitools.client.model.DataPageItemSchema;
import org.openapitools.client.model.ItemResponseSchema;
import org.openapitools.client.model.ItemType;

import hydra.GameConstants;
import hydra.dao.response.UseInCraftResponse;
import hydra.dao.util.Convertor;
import hydra.model.BotCraftSkill;
import hydra.model.BotItemDetails;
import hydra.model.BotItemReader;
import hydra.model.BotItemType;
import util.PermanentCacheManager;
import util.CacheManager;

public class ItemDAOImpl extends AbstractDAO implements ItemDAO {
	private static final String ITEMS_CACHE_KEY = "items";
	private CacheManager<String, UseInCraftResponse> craftCacheManager;
	private CacheManager<String, List<BotItemDetails>> itemCacheManager;
	private final ItemsApi itemsApi;

	public ItemDAOImpl(ApiClient apiClient) {
		itemsApi = new ItemsApi(apiClient);
		this.craftCacheManager = new PermanentCacheManager<>();
		this.itemCacheManager = new PermanentCacheManager<>();
	}

	@Override
	public List<BotItemDetails> getResourceItems() {
		if (itemCacheManager.contains(ITEMS_CACHE_KEY)) {
			return itemCacheManager.get(ITEMS_CACHE_KEY).stream()
					.filter(bid -> BotItemType.RESOURCE.equals(bid.getType())).toList();
		} else {
			try {
				ApiResponse<DataPageItemSchema> response = itemsApi.getAllItemsItemsGetWithHttpInfo(null, null, null,
						ItemType.RESOURCE, null, null, 1, 100);
				if (isOk(response)) {
					return response.getData().getData().stream()
							.map(item -> Convertor.convert(BotItemDetails.class, item)).toList();
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
	public List<BotItemDetails> getItems() {
		if (itemCacheManager.contains(ITEMS_CACHE_KEY)) {
			return itemCacheManager.get(ITEMS_CACHE_KEY);
		} else {
			List<BotItemDetails> items = getItems(null, null, null, null, null, null);
			if (items != null) {
				itemCacheManager.add(ITEMS_CACHE_KEY, items);
				initializeCraftUseCache(items);
			}
			return items;
		}
	}

	private void initializeCraftUseCache(List<BotItemDetails> items) {
		UseInCraftResponse falseResponse = new UseInCraftResponse(false, false);
		// On initialise tous le item à faux
		items.stream().forEach(bid -> craftCacheManager.add(bid.getCode(), falseResponse));
		UseInCraftResponse trueResponse = new UseInCraftResponse(false, true);
		// Mise à jour de l'autre cache
		for (BotItemDetails item : items) {
			if (item.getCraft() != null) {
				for (BotItemReader craftItem : item.getCraft().getItems()) {
					craftCacheManager.add(craftItem.getCode(), trueResponse);
				}
			}
		}
	}

	@Override
	public List<BotItemDetails> getItems(BotCraftSkill skill) {
		if (itemCacheManager.contains(ITEMS_CACHE_KEY)) {
			return itemCacheManager.get(ITEMS_CACHE_KEY).stream()
					.filter(item -> item.getCraft() != null && skill.equals(item.getCraft().getSkill())).toList();
		}
		return getItems(null, null, null, null, skill, null);
	}

	@Override
	public List<BotItemDetails> getItems(BotItemType type) {
		if (itemCacheManager.contains(ITEMS_CACHE_KEY)) {
			return itemCacheManager.get(ITEMS_CACHE_KEY).stream().filter(item -> type.equals(item.getType())).toList();
		}
		return getItems(null, null, null, type, null, null);
	}

	@Override
	public List<BotItemDetails> getItems(BotItemType type, Integer minLevel, Integer maxLevel) {
		if (itemCacheManager.contains(ITEMS_CACHE_KEY)) {
			return itemCacheManager.get(ITEMS_CACHE_KEY).stream().filter(
					item -> type.equals(item.getType()) && item.getLevel() >= minLevel && item.getLevel() <= maxLevel)
					.toList();
		}
		return getItems(minLevel, maxLevel, null, type, null, null);
	}

	@Override
	public List<BotItemDetails> getItems(Integer minLevel, Integer maxLevel) {
		if (itemCacheManager.contains(ITEMS_CACHE_KEY)) {
			return itemCacheManager.get(ITEMS_CACHE_KEY).stream()
					.filter(item -> item.getLevel() >= minLevel && item.getLevel() <= maxLevel).toList();
		}
		return getItems(minLevel, maxLevel, null, null, null, null);
	}

	@Override
	public List<BotItemDetails> getTaskItems() {
		List<BotItemDetails> resourceItems = getItems(BotItemType.RESOURCE);
		return resourceItems.stream().filter(bid -> GameConstants.SUBTYPE_RESOURCE_TASK.equals(bid.getSubtype()))
				.toList();
	}

	@Override
	public UseInCraftResponse useInCraft(String code) {
		if (!craftCacheManager.contains(code)) {
			getItems();
		}
		return craftCacheManager.get(code);
	}

	@Override
	public BotItemDetails getItem(String code) {
		if (itemCacheManager.contains(ITEMS_CACHE_KEY)) {
			return itemCacheManager.get(ITEMS_CACHE_KEY).stream().filter(item -> item.getCode().equals(code))
					.findFirst().get();
		}
		try {
			ApiResponse<ItemResponseSchema> response = itemsApi.getItemItemsCodeGetWithHttpInfo(code);
			if (isOk(response)) {
				return Convertor.convert(BotItemDetails.class, response.getData().getData());
			} else {
				return null;
			}
		} catch (ApiException e) {
			logError(e);
			return null;
		}
	}

	private List<BotItemDetails> getItems(Integer minLevel, Integer maxLevel, String code, BotItemType type,
			BotCraftSkill craftSkill, String craftMaterial) {
		List<BotItemDetails> currentResultList = new ArrayList<>();
		try {
			ApiResponse<DataPageItemSchema> response = itemsApi.getAllItemsItemsGetWithHttpInfo(minLevel, maxLevel,
					code, type != null ? ItemType.valueOf(type.name()) : null,
					craftSkill != null ? CraftSkill.valueOf(craftSkill.name()) : null, craftMaterial, 1, 100);
			if (isOk(response)) {
				currentResultList.addAll(response.getData().getData().stream()
						.map(item -> Convertor.convert(BotItemDetails.class, item)).toList());
				for (int i = 2; i <= response.getData().getPages(); i++) {
					response = itemsApi.getAllItemsItemsGetWithHttpInfo(minLevel, maxLevel, code,
							type != null ? ItemType.valueOf(type.name()) : null,
							craftSkill != null ? CraftSkill.valueOf(craftSkill.name()) : null, craftMaterial, i, 100);
					if (isOk(response)) {
						currentResultList.addAll(response.getData().getData().stream()
								.map(item -> Convertor.convert(BotItemDetails.class, item)).toList());
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
}
