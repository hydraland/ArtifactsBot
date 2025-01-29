package hydra.dao.response;

import hydra.model.BotFight;

public final record FightResponse(boolean ok, BotFight fight, boolean monsterNotFound) {
}
