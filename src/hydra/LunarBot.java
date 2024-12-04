package hydra;

import strategy.OptimisedTimeStrategyV2;
import strategy.Strategy;
import strategy.achiever.GoalExecutorManagerImpl;
import strategy.achiever.GoalParameter;
import strategy.achiever.GoalExecutoManager;
import strategy.achiever.factory.ArtifactGoalFactory;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.MonsterEquipementService;
import strategy.achiever.factory.MonsterEquipementServiceImpl;
import strategy.util.fight.FightService;
import strategy.util.fight.FightServiceImpl;

public final class LunarBot extends Bot {

	private static final int MIN_FREE_SLOT = 5;
	private static final int RARE_ITEM_SEUIL_RATE = 100;
	private static final int RESERVED_COINS = 12;
	private static final int MIN_FREE_INVENTORY_SPACE = 5;
	private final Strategy strategy;
	private final GoalExecutoManager goalExecutorManager;
	private final GoalParameter goalParameter;

	private LunarBot(String token) {
		super("Lunar", token);
		goalParameter = new GoalParameter(MIN_FREE_SLOT, RARE_ITEM_SEUIL_RATE, RESERVED_COINS, MIN_FREE_INVENTORY_SPACE, true);
		FightService fightService = new FightServiceImpl(characterDao, bankDao, itemDao, characterService, moveService, gameService);
		MonsterEquipementService monsterEquipementService = new MonsterEquipementServiceImpl(fightService);
		characterDao.addEquipmentChangeListener(monsterEquipementService);
		GoalFactory goalFactory = new ArtifactGoalFactory(resourceDAO, monsterDao, mapDao, itemDao, characterDao,
				grandExchangeDAO, bankDao, taskDao, goalParameter, gameService, characterService, moveService, fightService, monsterEquipementService);
		strategy = new OptimisedTimeStrategyV2(characterDao, itemDao, goalFactory, characterService, bankDao);
		goalExecutorManager = new GoalExecutorManagerImpl(strategy, characterDao, eventsDao, characterCache, interruptor);
	}

	@Override
	protected void run() {
		goalExecutorManager.execute();
	}

	public static void main(String[] args) {
		LunarBot bot = new LunarBot(args[0]);
		bot.launch();
	}
}