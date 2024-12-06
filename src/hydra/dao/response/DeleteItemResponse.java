package hydra.dao.response;

import hydra.model.BotItemReader;

public record DeleteItemResponse(boolean ok, BotItemReader item) {
}
