package hydra.dao.response;

import hydra.model.BotItemReader;

public final record DeleteItemResponse(boolean ok, BotItemReader item) {
}
