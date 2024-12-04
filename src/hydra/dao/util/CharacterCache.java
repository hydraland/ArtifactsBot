package hydra.dao.util;

import hydra.model.BotCharacter;

public interface CharacterCache {

	BotCharacter getCharacter();

	BotCharacter setCharacter(BotCharacter character);

	void reset();

}