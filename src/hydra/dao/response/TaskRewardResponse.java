package hydra.dao.response;

import hydra.model.BotRewards;

public final record TaskRewardResponse(boolean ok, BotRewards rewards) {

}
