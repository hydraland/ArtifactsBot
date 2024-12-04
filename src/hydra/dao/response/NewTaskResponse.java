package hydra.dao.response;

import hydra.model.BotTask;

public record NewTaskResponse(boolean ok, BotTask task) {

}
