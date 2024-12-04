package hydra.dao;

import java.util.List;

import hydra.dao.response.CancelTaskResponse;
import hydra.dao.response.NewTaskResponse;
import hydra.dao.response.TaskRewardResponse;
import hydra.dao.response.TradeTaskResponse;
import hydra.model.BotRewardDetail;
import hydra.model.BotTaskDetail;

public interface TaskDAO {

	NewTaskResponse newTask();

	TaskRewardResponse completeTask();

	TaskRewardResponse exchange();

	TradeTaskResponse trade(String code, int quantity);

	CancelTaskResponse cancelTask();

	List<BotTaskDetail> getAllTask();

	List<BotRewardDetail> getAllTaskReward();
}