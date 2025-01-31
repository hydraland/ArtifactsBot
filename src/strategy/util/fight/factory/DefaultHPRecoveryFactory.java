package strategy.util.fight.factory;

import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import strategy.util.CharacterService;

public final class DefaultHPRecoveryFactory implements HPRecoveryFactory {

	private final CharacterDAO characterDao;
	private final ItemDAO itemDAO;
	private final CharacterService characterService;
	private HPRecovery hpRecovery;

	public DefaultHPRecoveryFactory(CharacterDAO characterDao, ItemDAO itemDAO, CharacterService characterService) {
		this.characterDao = characterDao;
		this.itemDAO = itemDAO;
		this.characterService = characterService;
	}

	@Override
	public HPRecovery createHPRecovery() {
		if (hpRecovery == null) {
			hpRecovery = new HPRecoveryImpl(characterDao, itemDAO, characterService);
		}
		return hpRecovery;
	}
}
