package hydra;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.dao.simulate.SimulatorManagerImpl;
import hydra.model.BotCraftSkill;
import strategy.SimulatorStrategy;
import strategy.Strategy;
import strategy.achiever.GoalExecutoManager;
import strategy.achiever.GoalExecutorManagerImpl;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.ArtifactGoalFactory;
import strategy.achiever.factory.DefaultMonsterItemDropFactory;
import strategy.achiever.factory.DefaultMonsterTaskFactory;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.GoalFactory.GoalFilter;
import strategy.achiever.factory.GoalFactoryCreator;
import strategy.achiever.factory.GoalFactoryCreatorImpl;
import strategy.achiever.factory.ItemTaskFactory;
import strategy.achiever.factory.MonsterItemDropFactory;
import strategy.achiever.factory.MonsterItemDropUseSimulatorFactory;
import strategy.achiever.factory.MonsterTaskFactory;
import strategy.achiever.factory.MonsterTaskUseSimulatorFactory;
import strategy.achiever.factory.OptimizedItemTaskFactory;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.GoalAverageOptimizerImpl;
import strategy.util.CharacterService;
import strategy.util.MonsterEquipementService;
import strategy.util.MonsterEquipementServiceImpl;
import strategy.util.fight.FightService;
import strategy.util.fight.FightServiceImpl;
import strategy.util.fight.factory.DefaultHPRecoveryFactory;
import strategy.util.fight.factory.HPRecoveryFactory;
import strategy.util.fight.factory.HPRecoveryUseSimulatorFactory;

public final class PhenixBot extends Bot {

	private static final float TASK_MONSTER_COOK_OR_POTION_CREATE_PER_CENT = 0.3f;
	private static final int MIN_FREE_SLOT = 6;
	private static final int RARE_ITEM_SEUIL_RATE = 100;
	private static final int RESERVED_COINS = 14;
	private static final int MIN_FREE_INVENTORY_SPACE = 15;
	private final GoalParameter goalParameter;
	private final GoalExecutoManager goalExecutorManager;
	private final Strategy strategy;

	private PhenixBot(String token) {
		super("Phenix", token);
		goalParameter = new GoalParameter(MIN_FREE_SLOT, RARE_ITEM_SEUIL_RATE, RESERVED_COINS,
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
		SimulatorManager simulator = new SimulatorManagerImpl(botEvents -> Collections.emptyList());
		simulator.init(bankDao, characterDao.getCharacter(), eventsDao, itemDao, mapDao, monsterDao, resourceDAO,
				taskDao, false, new HashMap<>());
		simulator.save(false);
		simulator.load(false);

		GoalAverageOptimizer goalAverageOptimizer = new GoalAverageOptimizerImpl(characterDao);
		addFactoryToParameter(goalFactory, goalAverageOptimizer, characterDao, itemDao, goalFactoryCreator,
				characterService, goalParameter, false);
		GoalParameter simulatorParameter = new GoalParameter(MIN_FREE_SLOT, RARE_ITEM_SEUIL_RATE, RESERVED_COINS,
				MIN_FREE_INVENTORY_SPACE);
		GoalFactory simulatedFactory = createSimulatorFactory(simulator, simulatorParameter);
		Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals = goalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING, GoalFilter.ALL).stream()
				.filter(aga -> BotCraftSkill.COOKING.equals(aga.getBotCraftSkill())
						|| BotCraftSkill.ALCHEMY.equals(aga.getBotCraftSkill()))
				.collect(Collectors.toMap(GoalAchieverInfo::getItemCode, GoalAchieverInfo::getGoal));
		MonsterTaskFactory monsterTaskFactory = new MonsterTaskUseSimulatorFactory(
				goalFactory.createMonstersGoals(resp -> !resp.fight().isWin(), GoalFilter.ALL).stream()
						.collect(Collectors.toMap(MonsterGoalAchiever::getMonsterCode, Function.identity())),
				cookAndAlchemyGoals, bankDao, characterDao, goalFactoryCreator, characterService, simulator,
				simulatedFactory, TASK_MONSTER_COOK_OR_POTION_CREATE_PER_CENT);
		goalParameter.setMonsterTaskFactory(monsterTaskFactory);
		MonsterItemDropFactory monsterItemDropFactory = new MonsterItemDropUseSimulatorFactory(cookAndAlchemyGoals,
				bankDao, characterDao, goalFactoryCreator, characterService, simulator, simulatedFactory,
				TASK_MONSTER_COOK_OR_POTION_CREATE_PER_CENT);
		goalParameter.setMonsterItemDropFactory(monsterItemDropFactory);
		HPRecoveryFactory hpRecoveryFactory = new HPRecoveryUseSimulatorFactory(characterDao, itemDao, bankDao,
				moveService, characterService, simulator);
		goalParameter.setHPRecoveryFactory(hpRecoveryFactory);
		strategy = new SimulatorStrategy(simulator, characterDao, bankDao, goalFactory, characterService, itemService,
				simulatedFactory, goalAverageOptimizer);
		goalExecutorManager = new GoalExecutorManagerImpl(strategy, characterDao, eventsDao, characterCache,
				interruptor);
	}

	private static void addFactoryToParameter(GoalFactory goalFactory, GoalAverageOptimizer goalAverageOptimizer,
			CharacterDAO characterDao, ItemDAO itemDao, GoalFactoryCreator goalFactoryCreator,
			CharacterService characterService, GoalParameter goalParameter, boolean isForSimu) {
		Map<String, GoalAchieverInfo<ArtifactGoalAchiever>> itemGoalsMap = goalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING, GoalFilter.ALL).stream()
				.collect(Collectors.toMap(GoalAchieverInfo::getItemCode, Function.identity()));

		ItemTaskFactory itemTaskFactory = new OptimizedItemTaskFactory(characterDao, goalFactoryCreator, itemGoalsMap,
				characterService, goalAverageOptimizer);
		goalParameter.setItemTaskFactory(itemTaskFactory);
		// En mode simu on utilise les fabriques par défaut pour ne pas avoir de boucle
		// infinie
		if (isForSimu) {
			goalParameter.setHPRecoveryFactory(new DefaultHPRecoveryFactory(characterDao, itemDao, characterService));
			goalParameter.setMonsterTaskFactory(new DefaultMonsterTaskFactory(
					goalFactory.createMonstersGoals(resp -> !resp.fight().isWin(), GoalFilter.ALL),
					goalFactoryCreator));
			goalParameter.setMonsterItemDropFactory(new DefaultMonsterItemDropFactory(goalFactoryCreator));
		}
	}

	private GoalFactory createSimulatorFactory(SimulatorManager simulator, GoalParameter simulatorParameter) {
		GoalFactory goalFactory = simulator.createFactory(simulatorParameter);
		GoalAverageOptimizer goalAverageOptimizer = new GoalAverageOptimizerImpl(simulator.getCharacterDAOSimulator());
		addFactoryToParameter(goalFactory, goalAverageOptimizer, simulator.getCharacterDAOSimulator(),
				simulator.getItemDAOSimulator(), simulator.getGoalFactoryCreator(),
				simulator.getCharacterServiceSimulator(), simulatorParameter, true);
		return goalFactory;
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
