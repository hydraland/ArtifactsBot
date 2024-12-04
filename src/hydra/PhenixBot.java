package hydra;

import java.util.Collections;
import java.util.HashMap;

import hydra.dao.simulate.SimulatorManager;
import hydra.dao.simulate.SimulatorManagerImpl;
import strategy.HPRecoveryUseSimulator;
import strategy.SimulatorStrategy;
import strategy.Strategy;
import strategy.StrategySimulatorListener;
import strategy.achiever.GoalExecutoManager;
import strategy.achiever.GoalExecutorManagerImpl;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.ArtifactGoalFactory;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.MonsterEquipementService;
import strategy.achiever.factory.MonsterEquipementServiceImpl;
import strategy.util.fight.FightService;
import strategy.util.fight.FightServiceImpl;

public final class PhenixBot extends Bot {

	private static final int MIN_FREE_SLOT = 6;
	private static final int RARE_ITEM_SEUIL_RATE = 100;
	private static final int RESERVED_COINS = 14;
	private static final int MIN_FREE_INVENTORY_SPACE = 15;
	private final GoalParameter goalParameter;
	private final GoalExecutoManager goalExecutorManager;
	private final Strategy strategy;

	private PhenixBot(String token) {
		super("Phenix", token);
		goalParameter = new GoalParameter(MIN_FREE_SLOT, RARE_ITEM_SEUIL_RATE, RESERVED_COINS, MIN_FREE_INVENTORY_SPACE, true);
		FightService fightService = new FightServiceImpl(characterDao, bankDao, itemDao, characterService, moveService, gameService);
		fightService.sethPRecovery(new HPRecoveryUseSimulator(characterDao, itemDao, characterService));
		MonsterEquipementService monsterEquipementService = new MonsterEquipementServiceImpl(fightService);
		characterDao.addEquipmentChangeListener(monsterEquipementService);
		GoalFactory goalFactory = new ArtifactGoalFactory(resourceDAO, monsterDao, mapDao, itemDao, characterDao,
				grandExchangeDAO, bankDao, taskDao, goalParameter, gameService, characterService, moveService, fightService, monsterEquipementService);
		StrategySimulatorListener strategySimulatorListener = new StrategySimulatorListener();
		SimulatorManager simulator = new SimulatorManagerImpl(strategySimulatorListener, botEvents -> Collections.emptyList());
		simulator.init(bankDao, characterDao.getCharacter(), eventsDao, itemDao, mapDao, monsterDao, resourceDAO, taskDao, false, new HashMap<>());
		simulator.save(false);
		simulator.load(false);
		strategy = new SimulatorStrategy(simulator, strategySimulatorListener, characterDao, bankDao, goalFactory, characterService, gameService, goalParameter);
		goalExecutorManager = new GoalExecutorManagerImpl(strategy, characterDao, eventsDao, characterCache, interruptor);
	}

	@Override
	protected void run() {
		goalExecutorManager.execute();
	}

	public static void main(String[] args) {
		PhenixBot bot = new PhenixBot(args[0]);
		bot.launch();
	}
}
