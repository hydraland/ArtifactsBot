package hydra.dao;

import java.util.List;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.GrandExchangeApi;
import org.openapitools.client.api.MyCharactersApi;
import org.openapitools.client.model.DataPageGeOrderHistorySchema;
import org.openapitools.client.model.GECreateOrderTransactionResponseSchema;
import org.openapitools.client.model.GEOrderCreationrSchema;
import org.openapitools.client.model.GeOrderHistorySchema;

import hydra.GameConstants;
import hydra.dao.util.Convertor;
import hydra.dao.util.CharacterCache;
import hydra.dao.util.CooldownManager;
import hydra.model.BotCharacter;
import hydra.model.BotInventoryItem;
import hydra.model.BotItemReader;
import util.CacheManager;
import util.LimitedTimeCacheManager;

public class GrandExchangeDAOImpl extends AbstractDAO implements GrandExchangeDAO {
	private static final String SELL_CACHE_KEY = "sell";
	private static final int DEFAULT_PRICE = 100;
	private CooldownManager cooldownManager;
	private String persoName;
	private CharacterCache characterCache;
	private final GrandExchangeApi grandExchangeApi;
	private final MyCharactersApi myCharactersApi;
	private final CacheManager<String, Boolean> sellPossibleCache;

	public GrandExchangeDAOImpl(ApiClient apiClient, CooldownManager cooldownManager, String persoName,
			CharacterCache characterCache) {
		this.cooldownManager = cooldownManager;
		this.persoName = persoName;
		this.characterCache = characterCache;
		grandExchangeApi = new GrandExchangeApi(apiClient);
		myCharactersApi = new MyCharactersApi(apiClient);
		sellPossibleCache = new LimitedTimeCacheManager<>(GameConstants.MIN_COOLDOWN_IN_SECOND);
	}

	public boolean sell(String code, int quantity, int price) {
		if(price == 0) {
			return false;
		}
		cooldownManager.waitBeforeNextAction();
		try {
			GEOrderCreationrSchema geOrderCreationrSchema = new GEOrderCreationrSchema();
			geOrderCreationrSchema.code(code).quantity(quantity).setPrice(price);
			ApiResponse<GECreateOrderTransactionResponseSchema> response = myCharactersApi
					.actionGeCreateSellOrderMyNameActionGrandexchangeSellPostWithHttpInfo(persoName,
							geOrderCreationrSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return true;
			}
			return false;
		} catch (ApiException e) {
			logError(e);
			return false;
		}
	}

	@Override
	public int estimateItemPrice(String code, int characterGold) {
		try {
			ApiResponse<DataPageGeOrderHistorySchema> response = grandExchangeApi
					.getGeSellHistoryGrandexchangeHistoryCodeGetWithHttpInfo(code, null, null, 1, 100);
			List<GeOrderHistorySchema> hystoric = response.getData().getData();
			int sum = hystoric.stream().<Integer>map(h -> h.getPrice()).reduce(0, (a, b) -> a + b);
			int price = (sum == 0) ? DEFAULT_PRICE : sum / hystoric.size();
			return Math.min(price, Math.round(characterGold/GameConstants.SELL_TAXE_VALUE));
		} catch (ApiException e) {
			logError(e);
			return DEFAULT_PRICE;
		}
	}

	@Override
	public boolean sell(BotInventoryItem sellItem, int price) {
		return sell(sellItem.getCode(), sellItem.getQuantity(), price);
	}

	@Override
	public boolean sell(BotItemReader sellItem, int price) {
		return sell(sellItem.getCode(), sellItem.getQuantity(), price);
	}

	@Override
	public boolean isSellPossible() {
		if(sellPossibleCache.contains(SELL_CACHE_KEY)) {
			return sellPossibleCache.get(SELL_CACHE_KEY); 
		}
		try {
			boolean result = grandExchangeApi
					.getGeSellOrdersGrandexchangeOrdersGetWithHttpInfo(GameConstants.ACCOUNT_NAME, null, 1, 100).getData().getData()
					.size() < GameConstants.MAX_SELL_ORDER;
			sellPossibleCache.add(SELL_CACHE_KEY, result);
			return result;
		} catch (ApiException e) {
			logError(e);
			return false;
		}
	}
}
