package hydra;

import java.util.Map;
import java.util.stream.Collectors;

import strategy.BalanceTimeStrategy;
import strategy.achiever.GoalExecutoManager;
import strategy.achiever.GoalExecutorManagerImpl;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.ArtifactGoalFactory;
import strategy.achiever.factory.DefaultItemTaskFactory;
import strategy.achiever.factory.DefaultMonsterTaskFactory;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.GoalFactoryCreator;
import strategy.achiever.factory.GoalFactoryCreatorImpl;
import strategy.achiever.factory.ItemTaskFactory;
import strategy.achiever.factory.MonsterTaskFactory;
import strategy.achiever.factory.GoalFactory.GoalFilter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.util.MonsterEquipementService;
import strategy.util.MonsterEquipementServiceImpl;
import strategy.util.fight.FightService;
import strategy.util.fight.FightServiceImpl;
import strategy.util.fight.factory.DefaultHPRecoveryFactory;
import strategy.util.fight.factory.HPRecoveryFactory;

public final class SolarBot extends Bot {

	private static final int MIN_FREE_SLOT = 6;
	private static final int RARE_ITEM_SEUIL_RATE = 100;
	private static final int RESERVED_COINS = 5;
	private static final int MIN_FREE_INVENTORY_SPACE = 10;
	private final GoalExecutoManager goalExecutorManager;

	private SolarBot(String token) {
		super("Solar", token);
		GoalParameter goalParameter = new GoalParameter(MIN_FREE_SLOT, RARE_ITEM_SEUIL_RATE, RESERVED_COINS,
				MIN_FREE_INVENTORY_SPACE);
		FightService fightService = new FightServiceImpl(characterDao, bankDao, itemDao, characterService, moveService,
				itemService);
		MonsterEquipementService monsterEquipementService = new MonsterEquipementServiceImpl(fightService);
		characterDao.addEquipmentChangeListener(monsterEquipementService);
		GoalFactoryCreator goalFactoryCreator = new GoalFactoryCreatorImpl(characterDao, bankDao, itemDao,
				grandExchangeDAO, taskDao, mapDao, moveService, characterService, itemService, fightService,
				monsterEquipementService, goalParameter);
		GoalFactory goalFactory = new ArtifactGoalFactory(resourceDAO, monsterDao, mapDao, itemDao, characterDao,
				eventsDao, goalParameter, characterService, goalFactoryCreator);
		MonsterTaskFactory monsterTaskFactory = new DefaultMonsterTaskFactory(
				goalFactory.createMonstersGoals(resp -> !resp.fight().isWin(), GoalFilter.ALL), goalFactoryCreator);
		goalParameter.setMonsterTaskFactory(monsterTaskFactory);
		Map<String, ArtifactGoalAchiever> itemGoalsMap = goalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING, GoalFilter.ALL).stream()
				.collect(Collectors.toMap(GoalAchieverInfo::getItemCode, GoalAchieverInfo::getGoal));
		ItemTaskFactory itemTaskFactory = new DefaultItemTaskFactory(characterDao, goalFactoryCreator, itemGoalsMap,
				characterService);
		goalParameter.setItemTaskFactory(itemTaskFactory);
		HPRecoveryFactory hpRecoveryFactory = new DefaultHPRecoveryFactory(characterDao, itemDao, characterService);
		goalParameter.setHPRecoveryFactory(hpRecoveryFactory);
		BalanceTimeStrategy strategy = new BalanceTimeStrategy(characterDao, goalFactory, characterService, bankDao);
		goalExecutorManager = new GoalExecutorManagerImpl(strategy, characterDao, eventsDao, characterCache,
				interruptor);
	}

	@Override
	protected void run() {
		goalExecutorManager.execute();
	}

	public static void main(String[] args) {
		SolarBot bot = new SolarBot(args[0]);
		bot.launch();
	}
}