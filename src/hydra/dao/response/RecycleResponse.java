package hydra.dao.response;

import hydra.model.BotRecycleDetails;

public record RecycleResponse(boolean ok, BotRecycleDetails details) {

}
