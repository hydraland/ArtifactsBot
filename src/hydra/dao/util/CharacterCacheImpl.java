package hydra.dao.util;

import hydra.model.BotCharacter;

public final class CharacterCacheImpl implements CharacterCache {
	private BotCharacter character;

	@Override
	public final BotCharacter getCharacter() {
		return character;
	}

	@Override
	public final BotCharacter setCharacter(BotCharacter character) {
		this.character = character;
		return character;
	}
	
	@Override
	public void reset() {
		this.character = null;
	}
}
