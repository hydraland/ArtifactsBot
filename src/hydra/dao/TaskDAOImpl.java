package hydra.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.MyCharactersApi;
import org.openapitools.client.api.TasksApi;
import org.openapitools.client.model.DataPageDropRateSchema;
import org.openapitools.client.model.DataPageTaskFullSchema;
import org.openapitools.client.model.RewardDataResponseSchema;
import org.openapitools.client.model.SimpleItemSchema;
import org.openapitools.client.model.TaskCancelledResponseSchema;
import org.openapitools.client.model.TaskResponseSchema;
import org.openapitools.client.model.TaskTradeResponseSchema;

import hydra.dao.response.CancelTaskResponse;
import hydra.dao.response.NewTaskResponse;
import hydra.dao.response.TaskRewardResponse;
import hydra.dao.response.TradeTaskResponse;
import hydra.dao.util.CharacterCache;
import hydra.dao.util.Convertor;
import hydra.dao.util.CooldownManager;
import hydra.model.BotCharacter;
import hydra.model.BotRewardDetail;
import hydra.model.BotRewards;
import hydra.model.BotTask;
import hydra.model.BotTaskDetail;

public final class TaskDAOImpl extends AbstractDAO implements TaskDAO {
	private final String persoName;
	private final CooldownManager cooldownManager;
	private final CharacterCache characterCache;
	private final MyCharactersApi myCharactersApi;
	private final TasksApi taskApi;

	public TaskDAOImpl(ApiClient apiClient, String persoName, CooldownManager cooldownManager, CharacterCache characterCache) {
		this.persoName = persoName;
		this.cooldownManager = cooldownManager;
		this.characterCache = characterCache;
		myCharactersApi = new MyCharactersApi(apiClient);
		taskApi = new TasksApi(apiClient);
	}
	
	@Override
	public NewTaskResponse newTask() {
		cooldownManager.waitBeforeNextAction();
		try {
			ApiResponse<TaskResponseSchema> response = myCharactersApi
					.actionAcceptNewTaskMyNameActionTaskNewPostWithHttpInfo(persoName);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new NewTaskResponse(true,
						Convertor.convert(BotTask.class, response.getData().getData().getTask()));
			} else {
				return new NewTaskResponse(false, null);
			}
		} catch (ApiException e) {
			logError(e);
			return new NewTaskResponse(false, null);
		}
	}
	
	@Override
	public CancelTaskResponse cancelTask() {
		cooldownManager.waitBeforeNextAction();
		try {
			ApiResponse<TaskCancelledResponseSchema> response = myCharactersApi
					.actionTaskCancelMyNameActionTaskCancelPostWithHttpInfo(persoName);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new CancelTaskResponse(true);
			} else {
				return new CancelTaskResponse(false);
			}
		} catch (ApiException e) {
			logError(e);
			return new CancelTaskResponse(false);
		}
	}
	
	@Override
	public TaskRewardResponse completeTask() {
		cooldownManager.waitBeforeNextAction();
		try {
			ApiResponse<RewardDataResponseSchema> response = myCharactersApi
					.actionCompleteTaskMyNameActionTaskCompletePostWithHttpInfo(persoName);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new TaskRewardResponse(true,
						Convertor.convert(BotRewards.class, response.getData().getData().getRewards()));
			} else {
				return new TaskRewardResponse(false, null);
			}
		} catch (ApiException e) {
			logError(e);
			return new TaskRewardResponse(false, null);
		}
	}
	
	@Override
	public TaskRewardResponse exchange() {
		cooldownManager.waitBeforeNextAction();
		try {
			ApiResponse<RewardDataResponseSchema> response = myCharactersApi
					.actionTaskExchangeMyNameActionTaskExchangePostWithHttpInfo(persoName);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new TaskRewardResponse(true,
						Convertor.convert(BotRewards.class, response.getData().getData().getRewards()));
			} else {
				return new TaskRewardResponse(false, null);
			}
		} catch (ApiException e) {
			logError(e);
			return new TaskRewardResponse(false, null);
		}
	}
	
	@Override
	public TradeTaskResponse trade(String code, int quantity) {
		cooldownManager.waitBeforeNextAction();
		try {
			SimpleItemSchema simpleItemSchema = new SimpleItemSchema();
			simpleItemSchema.code(code).setQuantity(quantity);
			ApiResponse<TaskTradeResponseSchema> response = myCharactersApi.actionTaskTradeMyNameActionTaskTradePostWithHttpInfo(persoName, simpleItemSchema);
			if (isOk(response)) {
				cooldownManager.begin(response.getData().getData().getCooldown().getRemainingSeconds());
				characterCache.setCharacter(
						Convertor.convert(BotCharacter.class, response.getData().getData().getCharacter()));
				return new TradeTaskResponse(true);
			} else {
				return new TradeTaskResponse(false);
			}
		} catch (ApiException e) {
			logError(e);
			return new TradeTaskResponse(false);
		}
	}

	@Override
	public List<BotTaskDetail> getAllTask() {
		List<BotTaskDetail> currentResultList = new ArrayList<>();
		try {
			ApiResponse<DataPageTaskFullSchema> response = taskApi.getAllTasksTasksListGetWithHttpInfo(null, null, null, null, 1, 100);
			if (isOk(response)) {
				currentResultList.addAll(response.getData().getData().stream()
						.map(task -> Convertor.convert(BotTaskDetail.class, task)).toList());
				for (int i = 2; i <= response.getData().getPages(); i++) {
					response = taskApi.getAllTasksTasksListGetWithHttpInfo(null, null, null, null, i, 100);
					if (isOk(response)) {
						currentResultList.addAll(response.getData().getData().stream()
								.map(task -> Convertor.convert(BotTaskDetail.class, task)).toList());
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

	@Override
	public List<BotRewardDetail> getAllTaskReward() {
		List<BotRewardDetail> currentResultList = new ArrayList<>();
		try {
			ApiResponse<DataPageDropRateSchema> response = taskApi.getAllTasksRewardsTasksRewardsGetWithHttpInfo(1, 100);
			if (isOk(response)) {
				currentResultList.addAll(response.getData().getData().stream()
						.map(reward -> Convertor.convert(BotRewardDetail.class, reward)).toList());
				for (int i = 2; i <= response.getData().getPages(); i++) {
					response = taskApi.getAllTasksRewardsTasksRewardsGetWithHttpInfo(i, 100);
					if (isOk(response)) {
						currentResultList.addAll(response.getData().getData().stream()
								.map(reward -> Convertor.convert(BotRewardDetail.class, reward)).toList());
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
