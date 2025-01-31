package strategy.util.fight.factory;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.dao.simulate.SimulatorManager;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public final class HPRecoveryUseSimulatorFactory implements HPRecoveryFactory {

	private final CharacterDAO characterDao;
	private final ItemDAO itemDAO;
	private final CharacterService characterService;
	private final BankDAO bankDAO;
	private final MoveService moveService;
	private final SimulatorManager simulatorManager;
	private HPRecovery hpRecovery;

	public HPRecoveryUseSimulatorFactory(CharacterDAO characterDao, ItemDAO itemDAO, BankDAO bankDAO,
			MoveService moveService, CharacterService characterService, SimulatorManager simulatorManager) {
		this.characterDao = characterDao;
		this.itemDAO = itemDAO;
		this.bankDAO = bankDAO;
		this.moveService = moveService;
		this.characterService = characterService;
		this.simulatorManager = simulatorManager;
	}

	@Override
	public HPRecovery createHPRecovery() {
		if (hpRecovery == null) {
			hpRecovery = new HPRecoveryUseSimulator(characterDao, itemDAO, bankDAO, moveService, characterService,
					simulatorManager);
		}
		return hpRecovery;
	}
}
