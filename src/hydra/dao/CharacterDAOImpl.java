package hydra.dao;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.CharactersApi;
import org.openapitools.client.api.MyCharactersApi;
import org.openapitools.client.model.CharacterFightResponseSchema;
import org.openapitools.client.model.CharacterMovementResponseSchema;
import org.openapitools.client.model.CharacterResponseSchema;
import org.openapitools.client.model.CharacterRestResponseSchema;
import org.openapitools.client.model.CraftingSchema;
import org.openapitools.client.model.DeleteItemResponseSchema;
import org.openapitools.client.model.DestinationSchema;
import org.openapitools.client.model.EquipSchema;
import org.openapitools.client.model.EquipmentResponseSchema;
import org.openapitools.client.model.ItemSlot;
import org.openapitools.client.model.RecyclingResponseSchema;
import org.openapitools.client.model.RecyclingSchema;
import org.openapitools.client.model.SimpleItemSchema;
import org.openapitools.client.model.SkillResponseSchema;
import org.openapitools.client.model.UnequipSchema;
import org.openapitools.client.model.UseItemResponseSchema;

import hydra.dao.response.CraftResponse;
import hydra.dao.response.DeleteItemResponse;
import hydra.dao.response.EquipResponse;
import hydra.dao.response.FightResponse;
import hydra.dao.response.GatheringResponse;
import hydra.dao.response.MoveResponse;
import hydra.dao.response.RecycleResponse;
import hydra.dao.response.RestResponse;
import hydra.dao.response.UseResponse;
import hydra.dao.util.CharacterCache;
import hydra.dao.util.Convertor;
import hydra.dao.util.CooldownManager;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotFight;
import hydra.model.BotGatheringDetails;
import hydra.model.BotItem;
import hydra.model.BotItemReader;
import hydra.model.BotRecycleDetails;
import strategy.achiever.Interruptor;
import util.EventListener;
import util.ListenerAdapter;

public class CharacterDAOImpl extends AbstractDAO implements CharacterDAO {
	private String persoName;
	private CooldownManager cooldownManager;
	private CharacterCache characterCache;
	private final MyCharactersApi myCharactersApi;
	private static final int ALREADY_IN_BOX = 490;
	private static final int NOT_FOUND_CODE = 598;
	private final CharactersApi charactersApi;
	private final Interruptor interruptor;
	private final ListenerAdapter<String> listenerAdapter;

	public CharacterDAOImpl(ApiClient apiClient, String persoName, CooldownManager cooldownManager,
			CharacterCache characterCache, Interruptor interruptor) {
		this.characterCache = characterCache;
		this.interruptor = interruptor;
		this.myCharactersApi = new MyCharactersApi(apiClient);
		this.charactersApi = new CharactersApi(apiClient);
		this.persoName = persoName;
		this.cooldownManager = cooldownManager;
		this.listenerAdapter = new ListenerAdapter<>();
	}
	
	@Override
	public void addEquipmentChangeListener(EventListener<String> listener) {
		listenerAdapter.addEventListener(listener);
	}
	
	@Override
	public void removeEquipmentChangeListener(EventListener<String> listener) {
		listenerAdapter.removeEventListener(listener);
	}

	@Override
	public MoveResponse move(int x, int y) {
		if(interruptor.isInterrupted()) {
			return new MoveResponse(false);
		}
		cooldownManager.waitBeforeNextAction();
		DestinationSchema destinationSchema = new DestinationSchema();
		destinationSchema.setX(x);
		destinationSchema.setY(y);
		try {
			ApiResponse<CharacterMovementResponseSchema> response = myCharactersApi
					.actionMoveMyNameActionMovePostWithHttpInfo(persoName, destinationSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new MoveResponse(isOk(response) || response.getStatusCode() == ALREADY_IN_BOX);
			} else if (response.getStatusCode() == ALREADY_IN_BOX) {
				return new MoveResponse(true);
			} else {
				return new MoveResponse(false);
			}
		} catch (ApiException e) {
			logError(e);
			if (e.getCode() == ALREADY_IN_BOX) {
				return new MoveResponse(true);
			} else {
				return new MoveResponse(false);
			}
		}
	}

	@Override
	public FightResponse fight() {
		if(interruptor.isInterrupted()) {
			return new FightResponse(false, null, false);
		}
		cooldownManager.waitBeforeNextAction();
		try {
			ApiResponse<CharacterFightResponseSchema> response = myCharactersApi
					.actionFightMyNameActionFightPostWithHttpInfo(persoName);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new FightResponse(true,
						Convertor.convert(BotFight.class, response.getData().getData().getFight()), false);
			} else {
				return new FightResponse(false, null, response.getStatusCode() == NOT_FOUND_CODE);
			}
		} catch (ApiException e) {
			logError(e);
			return new FightResponse(false, null, e.getCode() == NOT_FOUND_CODE);
		}
	}

	@Override
	public RestResponse rest() {
		cooldownManager.waitBeforeNextAction();
		try {
			ApiResponse<CharacterRestResponseSchema> response = myCharactersApi
					.actionRestMyNameActionRestPostWithHttpInfo(persoName);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new RestResponse(true, response.getData().getData().getHpRestored());
			} else {
				return new RestResponse(false, 0);
			}
		} catch (ApiException e) {
			logError(e);
			return new RestResponse(false, 0);
		}
	}
	
	@Override
	public UseResponse use(String code, int quantity) {
		cooldownManager.waitBeforeNextAction();
		try {
			SimpleItemSchema simpleItemSchema = new SimpleItemSchema();
			simpleItemSchema.code(code).setQuantity(quantity);
			ApiResponse<UseItemResponseSchema> response = myCharactersApi
					.actionUseItemMyNameActionUsePostWithHttpInfo(persoName, simpleItemSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new UseResponse(true);
			} else {
				return new UseResponse(false);
			}
		} catch (ApiException e) {
			logError(e);
			return new UseResponse(false);
		}
	}

	@Override
	public EquipResponse equip(String code, BotCharacterInventorySlot slot, int quantity) {
		cooldownManager.waitBeforeNextAction();
		EquipSchema equipSchema = new EquipSchema();
		equipSchema.code(code).quantity(quantity).setSlot(ItemSlot.valueOf(slot.name()));
		try {
			ApiResponse<EquipmentResponseSchema> response = myCharactersApi
					.actionEquipItemMyNameActionEquipPostWithHttpInfo(persoName, equipSchema);
			if (isOk(response)) {
				listenerAdapter.fire("equip");
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new EquipResponse(true);
			} else {
				return new EquipResponse(false);
			}
		} catch (ApiException e) {
			logError(e);
			return new EquipResponse(false);
		}
	}

	@Override
	public CraftResponse craft(String code, int quantity) {
		cooldownManager.waitBeforeNextAction();
		CraftingSchema craftingSchema = new CraftingSchema();
		craftingSchema.code(code).setQuantity(quantity);
		try {
			ApiResponse<SkillResponseSchema> response = myCharactersApi
					.actionCraftingMyNameActionCraftingPostWithHttpInfo(persoName, craftingSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new CraftResponse(true);
			} else {
				return new CraftResponse(false);
			}
		} catch (ApiException e) {
			logError(e);
			return new CraftResponse(false);
		}
	}

	@Override
	public RecycleResponse recycle(String code, int quantity) {
		cooldownManager.waitBeforeNextAction();
		RecyclingSchema recyclingSchema = new RecyclingSchema();
		recyclingSchema.code(code).setQuantity(quantity);
		try {
			ApiResponse<RecyclingResponseSchema> response = myCharactersApi
					.actionRecyclingMyNameActionRecyclingPostWithHttpInfo(persoName, recyclingSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new RecycleResponse(true,
						Convertor.convert(BotRecycleDetails.class, response.getData().getData().getDetails()));
			} else {
				return new RecycleResponse(false, null);
			}
		} catch (ApiException e) {
			logError(e);
			return new RecycleResponse(false, null);
		}
	}

	@Override
	public GatheringResponse collect() {
		if(interruptor.isInterrupted()) {
			return new GatheringResponse(false, null, false);
		}
		cooldownManager.waitBeforeNextAction();
		try {
			ApiResponse<SkillResponseSchema> response = myCharactersApi
					.actionGatheringMyNameActionGatheringPostWithHttpInfo(persoName);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new GatheringResponse(true,
						Convertor.convert(BotGatheringDetails.class, response.getData().getData().getDetails()), false);
			} else {
				return new GatheringResponse(false, null, response.getStatusCode() == NOT_FOUND_CODE);
			}
		} catch (ApiException e) {
			logError(e);
			return new GatheringResponse(false, null, e.getCode() == NOT_FOUND_CODE);
		}
	}

	@Override
	public EquipResponse unequip(BotCharacterInventorySlot slot, int quantity) {
		cooldownManager.waitBeforeNextAction();
		UnequipSchema unequipSchema = new UnequipSchema();
		unequipSchema.slot(ItemSlot.valueOf(slot.name()))
				.setQuantity(quantity);
		try {
			ApiResponse<EquipmentResponseSchema> response = myCharactersApi
					.actionUnequipItemMyNameActionUnequipPostWithHttpInfo(persoName, unequipSchema);
			if (isOk(response)) {
				listenerAdapter.fire("unequip");
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new EquipResponse(true);
			} else {
				return new EquipResponse(false);
			}
		} catch (ApiException e) {
			logError(e);
			return new EquipResponse(false);
		}
	}

	@Override
	public BotCharacter getCharacter() {
		if (characterCache.getCharacter() != null) {
			return characterCache.getCharacter();
		}
		cooldownManager.waitBeforeNextAction();
		try {
			ApiResponse<CharacterResponseSchema> response = charactersApi
					.getCharacterCharactersNameGetWithHttpInfo(persoName);
			if (isOk(response)) {
				return characterCache.setCharacter(Convertor.convert(BotCharacter.class, response.getData().getData()));
			} else {
				return null;
			}
		} catch (ApiException e) {
			logError(e);
			return null;
		}
	}

	@Override
	public DeleteItemResponse deleteItem(BotItemReader item) {
		cooldownManager.waitBeforeNextAction();
		try {
			SimpleItemSchema simpleItemSchema = new SimpleItemSchema();
			simpleItemSchema.code(item.getCode()).setQuantity(item.getQuantity());
			ApiResponse<DeleteItemResponseSchema> response = myCharactersApi
					.actionDeleteItemMyNameActionDeletePostWithHttpInfo(persoName, simpleItemSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new DeleteItemResponse(true,
						Convertor.convert(BotItem.class, response.getData().getData().getItem()));
			} else {
				return new DeleteItemResponse(false, null);
			}
		} catch (ApiException e) {
			logError(e);
			return new DeleteItemResponse(false, null);
		}
	}
}
