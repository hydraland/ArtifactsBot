package hydra.dao.response;

import hydra.model.BotRecycleDetails;

public final record RecycleResponse(boolean ok, BotRecycleDetails details) {

}
