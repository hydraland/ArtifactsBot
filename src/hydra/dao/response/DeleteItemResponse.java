package hydra.dao.response;

import hydra.model.BotItem;

public record DeleteItemResponse(boolean ok, BotItem item) {
}
