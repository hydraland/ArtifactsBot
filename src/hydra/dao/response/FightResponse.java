package hydra.dao.response;

import hydra.model.BotFight;

public record FightResponse(boolean ok, BotFight fight, boolean monsterNotFound) {
}
