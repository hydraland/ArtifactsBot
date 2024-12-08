package hydra.dao;

import java.util.List;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.MyAccountApi;
import org.openapitools.client.api.MyCharactersApi;
import org.openapitools.client.model.BankExtensionTransactionResponseSchema;
import org.openapitools.client.model.BankGoldTransactionResponseSchema;
import org.openapitools.client.model.BankItemTransactionResponseSchema;
import org.openapitools.client.model.BankResponseSchema;
import org.openapitools.client.model.DepositWithdrawGoldSchema;
import org.openapitools.client.model.SimpleItemSchema;

import hydra.GameConstants;
import hydra.dao.util.CharacterCache;
import hydra.dao.util.Convertor;
import hydra.dao.util.CooldownManager;
import hydra.model.BotBankDetail;
import hydra.model.BotCharacter;
import hydra.model.BotInventoryItem;
import hydra.model.BotItem;
import hydra.model.BotItemReader;
import strategy.util.BankRecorder;
import util.CacheManager;
import util.LimitedTimeCacheManager;

public class BankDAOImpl extends AbstractDAO implements BankDAO {

	private static final String BANK_DETAIL_CACHE_KEY = "detail";
	private String persoName;
	private CooldownManager cooldownManager;
	private BankRecorder bankRecorder;
	private CharacterCache characterCache;
	private final MyCharactersApi myCharactersApi;
	private final MyAccountApi myAccountApi;
	private final CacheManager<String, BotBankDetail> bankDetailCache;
	private static final BotBankDetail ERROR_BANK_DETAIL = new BotBankDetail();
	static {
		ERROR_BANK_DETAIL.setExpansions(0);
		ERROR_BANK_DETAIL.setGold(0);
		ERROR_BANK_DETAIL.setSlots(0);
		ERROR_BANK_DETAIL.setNextExpansionCost(Integer.MAX_VALUE);
	}

	public BankDAOImpl(ApiClient apiClient, String persoName, CooldownManager cooldownManager, BankRecorder bankRecorder,
			CharacterCache characterCache) {
		this.persoName = persoName;
		this.cooldownManager = cooldownManager;
		this.characterCache = characterCache;
		myCharactersApi = new MyCharactersApi(apiClient);
		myAccountApi = new MyAccountApi(apiClient);
		this.bankRecorder = bankRecorder;
		bankDetailCache = new LimitedTimeCacheManager<>(GameConstants.MIN_COOLDOWN_IN_SECOND);
	}

	@Override
	public boolean deposit(BotInventoryItem item) {
		cooldownManager.waitBeforeNextAction();
		SimpleItemSchema simpleItemSchema = new SimpleItemSchema();
		simpleItemSchema.code(item.getCode()).setQuantity(item.getQuantity());
		try {
			ApiResponse<BankItemTransactionResponseSchema> response = myCharactersApi
					.actionDepositBankMyNameActionBankDepositPostWithHttpInfo(persoName, simpleItemSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				BotItem botItem = new BotItem();
				botItem.setCode(item.getCode());
				botItem.setQuantity(item.getQuantity());
				bankRecorder.putItem(botItem);
				return true;
			} else {
				return false;
			}
		} catch (ApiException e) {
			logError(e);
			return false;
		}
	}
	
	@Override
	public boolean depositGold(int quantity) {
		cooldownManager.waitBeforeNextAction();
		try {
			DepositWithdrawGoldSchema depositWithdrawGoldSchema = new DepositWithdrawGoldSchema();
			depositWithdrawGoldSchema.setQuantity(quantity);
			ApiResponse<BankGoldTransactionResponseSchema> response = myCharactersApi
					.actionDepositBankGoldMyNameActionBankDepositGoldPostWithHttpInfo(persoName, depositWithdrawGoldSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return true;
			} else {
				return false;
			}
		} catch (ApiException e) {
			logError(e);
			return false;
		}
	}

	@Override
	public boolean withdraw(BotItemReader item) {
		if(item.getQuantity() > bankRecorder.getItem(item.getCode()).getQuantity()) {
			return false;
		}
		cooldownManager.waitBeforeNextAction();
		SimpleItemSchema simpleItemSchema = new SimpleItemSchema();
		simpleItemSchema.code(item.getCode()).setQuantity(item.getQuantity());
		try {
			ApiResponse<BankItemTransactionResponseSchema> response = myCharactersApi
					.actionWithdrawBankMyNameActionBankWithdrawPostWithHttpInfo(persoName, simpleItemSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				bankRecorder.remove(item);
				return true;
			} else {
				return false;
			}
		} catch (ApiException e) {
			logError(e);
			return false;
		}
	}
	
	@Override
	public boolean withdrawGold(int quantity) {
		cooldownManager.waitBeforeNextAction();
		try {
			DepositWithdrawGoldSchema depositWithdrawGoldSchema = new DepositWithdrawGoldSchema();
			depositWithdrawGoldSchema.setQuantity(quantity);
			ApiResponse<BankGoldTransactionResponseSchema> response = myCharactersApi
					.actionWithdrawBankGoldMyNameActionBankWithdrawGoldPostWithHttpInfo(persoName, depositWithdrawGoldSchema );
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return true;
			} else {
				return false;
			}
		} catch (ApiException e) {
			logError(e);
			return false;
		}
	}
	
	public BotBankDetail getBankDetail() {
		if(bankDetailCache.contains(BANK_DETAIL_CACHE_KEY)) {
			return bankDetailCache.get(BANK_DETAIL_CACHE_KEY);
		}
		try {
			ApiResponse<BankResponseSchema> response = myAccountApi.getBankDetailsMyBankGetWithHttpInfo();
			if (isOk(response)) {
				BotBankDetail result = Convertor.convert(BotBankDetail.class, response.getData().getData());
				bankDetailCache.add(BANK_DETAIL_CACHE_KEY, result);
				return result;
			} else {
				return ERROR_BANK_DETAIL;// cas d'erreur
			}
		} catch (ApiException e) {
			logError(e);
			return ERROR_BANK_DETAIL;// cas d'erreur
		}
	}

	@Override
	public boolean buyExtension() {
		cooldownManager.waitBeforeNextAction();
		try {
			ApiResponse<BankExtensionTransactionResponseSchema> response = myCharactersApi
					.actionBuyBankExpansionMyNameActionBankBuyExpansionPostWithHttpInfo(persoName);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return true;
			} else {
				return false;
			}
		} catch (ApiException e) {
			logError(e);
			return false;
		}
	}

	@Override
	public List<? extends BotItemReader> viewItems() {
		return bankRecorder.viewItems();
	}

	@Override
	public BotItemReader getItem(String code) {
		return bankRecorder.getItem(code);
	}
}
