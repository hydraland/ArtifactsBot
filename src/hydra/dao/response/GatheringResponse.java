package hydra.dao.response;

import hydra.model.BotGatheringDetails;

public final record GatheringResponse(boolean ok, BotGatheringDetails botDetails, boolean resourceNotFound) {
}
