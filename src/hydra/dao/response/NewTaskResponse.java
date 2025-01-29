package hydra.dao.response;

import hydra.model.BotTask;

public final record NewTaskResponse(boolean ok, BotTask task) {

}
