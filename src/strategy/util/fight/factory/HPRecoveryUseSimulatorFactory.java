package strategy.util.fight.factory;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.dao.simulate.SimulatorManager;
import strategy.StrategySimulatorListener;
import strategy.util.CharacterService;
import strategy.util.MoveService;
import strategy.util.fight.HPRecovery;

public class HPRecoveryUseSimulatorFactory implements HPRecoveryFactory {

	private final CharacterDAO characterDao;
	private final ItemDAO itemDAO;
	private final CharacterService characterService;
	private final BankDAO bankDAO;
	private final MoveService moveService;
	private final StrategySimulatorListener simulatorListener;
	private final SimulatorManager simulatorManager;

	public HPRecoveryUseSimulatorFactory(CharacterDAO characterDao, ItemDAO itemDAO, BankDAO bankDAO,
			MoveService moveService, CharacterService characterService, StrategySimulatorListener simulatorListener,
			SimulatorManager simulatorManager) {
		this.characterDao = characterDao;
		this.itemDAO = itemDAO;
		this.bankDAO = bankDAO;
		this.moveService = moveService;
		this.characterService = characterService;
		this.simulatorListener = simulatorListener;
		this.simulatorManager = simulatorManager;
	}

	@Override
	public HPRecovery createHPRecovery() {
		return new HPRecoveryUseSimulator(characterDao, itemDAO, bankDAO, moveService, characterService,
				simulatorListener, simulatorManager);
	}
}
