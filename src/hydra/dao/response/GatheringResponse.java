package hydra.dao.response;

import hydra.model.BotGatheringDetails;

public record GatheringResponse(boolean ok, BotGatheringDetails botDetails, boolean resourceNotFound) {
}
