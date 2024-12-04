package hydra.dao.response;

import hydra.model.BotRewards;

public record TaskRewardResponse(boolean ok, BotRewards rewards) {

}
