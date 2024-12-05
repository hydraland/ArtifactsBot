package strategy.util.fight.factory;

import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import strategy.util.CharacterService;
import strategy.util.fight.HPRecovery;

public class DefaultHPRecoveryFactory implements HPRecoveryFactory {

	private final CharacterDAO characterDao;
	private final ItemDAO itemDAO;
	private final CharacterService characterService;

	public DefaultHPRecoveryFactory(CharacterDAO characterDao, ItemDAO itemDAO, CharacterService characterService) {
		this.characterDao = characterDao;
		this.itemDAO = itemDAO;
		this.characterService = characterService;
	}

	@Override
	public HPRecovery createHPRecovery() {
		return new HPRecoveryImpl(characterDao, itemDAO, characterService);
	}
}
