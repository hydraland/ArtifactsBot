package hydra;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import strategy.OptimisedTimeStrategyV2;
import strategy.achiever.GoalExecutoManager;
import strategy.achiever.GoalExecutorManagerImpl;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.ArtifactGoalFactory;
import strategy.achiever.factory.DefaultMonsterTaskFactory;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.ItemTaskFactory;
import strategy.achiever.factory.MonsterTaskFactory;
import strategy.achiever.factory.OptimizedItemTaskFactory;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.GoalAverageOptimizerImpl;
import strategy.util.MonsterEquipementService;
import strategy.util.MonsterEquipementServiceImpl;
import strategy.util.fight.FightService;
import strategy.util.fight.FightServiceImpl;
import strategy.util.fight.factory.DefaultHPRecoveryFactory;
import strategy.util.fight.factory.HPRecoveryFactory;

public final class LunarBot extends Bot {

	private static final int MIN_FREE_SLOT = 5;
	private static final int RARE_ITEM_SEUIL_RATE = 100;
	private static final int RESERVED_COINS = 12;
	private static final int MIN_FREE_INVENTORY_SPACE = 5;
	private final GoalExecutoManager goalExecutorManager;

	private LunarBot(String token) {
		super("Lunar", token);
		GoalParameter goalParameter = new GoalParameter(MIN_FREE_SLOT, RARE_ITEM_SEUIL_RATE, RESERVED_COINS,
				MIN_FREE_INVENTORY_SPACE);
		FightService fightService = new FightServiceImpl(characterDao, bankDao, itemDao, characterService, moveService,
				gameService);
		MonsterEquipementService monsterEquipementService = new MonsterEquipementServiceImpl(fightService);
		characterDao.addEquipmentChangeListener(monsterEquipementService);
		GoalFactory goalFactory = new ArtifactGoalFactory(resourceDAO, monsterDao, mapDao, itemDao, characterDao,
				grandExchangeDAO, bankDao, taskDao, goalParameter, gameService, characterService, moveService,
				fightService, monsterEquipementService);
		MonsterTaskFactory monsterTaskFactory = new DefaultMonsterTaskFactory(
				goalFactory.createMonstersGoals(resp -> !resp.fight().isWin()), bankDao, characterDao, moveService,
				characterService, goalParameter);
		goalParameter.setMonsterTaskFactory(monsterTaskFactory);
		Map<String, ArtifactGoalAchiever> itemGoalsMap = goalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING).stream()
				.collect(Collectors.toMap(aga -> goalFactory.getInfos(aga).getItemCode(), Function.identity()));
		GoalAverageOptimizer goalAverageOptimizer = new GoalAverageOptimizerImpl(characterDao);
		ItemTaskFactory itemTaskFactory = new OptimizedItemTaskFactory(characterDao, taskDao, bankDao, itemGoalsMap,
				characterService, moveService, goalAverageOptimizer, goalParameter);
		goalParameter.setItemTaskFactory(itemTaskFactory);
		HPRecoveryFactory hpRecoveryFactory = new DefaultHPRecoveryFactory(characterDao, itemDao, characterService);
		goalParameter.setHPRecoveryFactory(hpRecoveryFactory);
		OptimisedTimeStrategyV2 strategy = new OptimisedTimeStrategyV2(characterDao, itemDao, goalFactory, characterService, bankDao,
				goalAverageOptimizer);
		goalExecutorManager = new GoalExecutorManagerImpl(strategy, characterDao, eventsDao, characterCache,
				interruptor);
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