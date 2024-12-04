package hydra.dao.simulate;

import java.io.Serializable;
import java.util.List;

import hydra.model.BotRewardDetail;
import hydra.model.BotTaskDetail;

public final class TaskStruct implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<BotTaskDetail> botTaskDetails;
	private List<BotRewardDetail> botRewardDetails;
	
	public TaskStruct() {
	}
	
	public TaskStruct(List<BotTaskDetail> botTaskDetails, List<BotRewardDetail> botRewardDetails) {
		this.botTaskDetails = botTaskDetails;
		this.botRewardDetails = botRewardDetails;
	}

	public final List<BotTaskDetail> getBotTaskDetails() {
		return botTaskDetails;
	}

	public final void setBotTaskDetails(List<BotTaskDetail> botTaskDetails) {
		this.botTaskDetails = botTaskDetails;
	}

	public final List<BotRewardDetail> getBotRewardDetails() {
		return botRewardDetails;
	}

	public final void setBotRewardDetails(List<BotRewardDetail> botRewardDetails) {
		this.botRewardDetails = botRewardDetails;
	}
}