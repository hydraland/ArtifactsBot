package hydra.dao.simulate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotItem;
import hydra.model.BotItemReader;
import strategy.Strategy;
import strategy.StrategySimulatorListener;
import strategy.SumAccumulator;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.MonsterTaskUseSimulatorFactory;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.GoalAverageOptimizerImpl;
import strategy.util.Bornes;
import strategy.util.StrategySkillUtils;
import strategy.util.fight.factory.DefaultHPRecoveryFactory;
import strategy.util.fight.factory.HPRecoveryUseSimulatorFactory;
import util.JsonToStringStyle;

public class TestSimulation {
	public static void main(String[] args) throws FileNotFoundException {
		LogManager.getLogManager().getLogger("").setLevel(Level.SEVERE);
		ToStringBuilder.setDefaultStyle(new JsonToStringStyle());
		// int[] val = new int[1];
		StrategySimulatorListener simulatorListener = new StrategySimulatorListener();
		simulatorListener.setInnerListener((className, methodName, cooldown, error) -> System.out.println(methodName));
		SimulatorManagerImpl simulatorManager = new SimulatorManagerImpl(simulatorListener,
				botEvents -> new ArrayList<>());
		simulatorManager.load(true);

		BotCharacter character = simulatorManager.getCharacterDAOSimulator().getCharacter();
		System.setOut(new PrintStream(new File("test.txt")));
		GoalParameter goalParameter = new GoalParameter(6, 100, 14, 15);
		GoalFactory simulatedGoalFactory = simulatorManager.createFactory(goalParameter);
		goalParameter.setHPRecoveryFactory(new DefaultHPRecoveryFactory(simulatorManager.getCharacterDAOSimulator(),
				simulatorManager.getItemDAOSimulator(), simulatorManager.getCharacterServiceSimulator()));
		List<GoalAchieverInfo> dropItemGoal = simulatedGoalFactory.getDropItemGoal();
		/*
		 * character.setX(0); character.setY(-2);
		 * simulatorManager.getCharacterDAOSimulator().fight();
		 */
		//simulateCrafting(simulatorListener, simulatorManager, character, simulatedGoalFactory);
		//simulateFight(simulatorListener, simulatorManager, character, simulatedGoalFactory);

		simulateCookingAndFight(simulatorListener, simulatorManager, character, simulatedGoalFactory, goalParameter);
	}

	private static void simulateCookingAndFight(StrategySimulatorListener simulatorListener,
			SimulatorManagerImpl simulatorManager, BotCharacter character, GoalFactory simulatedGoalFactory,
			GoalParameter goalParameter) {

		StrategySimulatorListener secondSimulatorListener = new StrategySimulatorListener();
		secondSimulatorListener
				.setInnerListener((className, methodName, cooldown, error) -> System.out.println(methodName));
		SimulatorManager secondSimulatorManager = new SimulatorManagerImpl(secondSimulatorListener,
				botEvents -> new ArrayList<>());
		secondSimulatorManager.load(true);

		goalParameter.setHPRecoveryFactory(new HPRecoveryUseSimulatorFactory(
				simulatorManager.getCharacterDAOSimulator(), simulatorManager.getItemDAOSimulator(),
				simulatorManager.getBankDAOSimulator(), simulatorManager.getMoveService(),
				simulatorManager.getCharacterServiceSimulator(), secondSimulatorListener, secondSimulatorManager));

		GoalParameter simulateGoalParameter = new GoalParameter(goalParameter.getMinFreeSlot(),
				goalParameter.getRareItemSeuil(), goalParameter.getCoinReserve(),
				goalParameter.getMinFreeInventorySpace());
		simulateGoalParameter.setHPRecoveryFactory(new DefaultHPRecoveryFactory(
				secondSimulatorManager.getCharacterDAOSimulator(), secondSimulatorManager.getItemDAOSimulator(),
				secondSimulatorManager.getCharacterServiceSimulator()));
		GoalFactory secondSimulatedGoalFactory = secondSimulatorManager.createFactory(simulateGoalParameter);

		List<MonsterGoalAchiever> monsterGoals = simulatedGoalFactory
				.createMonstersGoals(resp -> !resp.fight().isWin());

		Collection<GoalAchieverInfo> itemSimulatedGoals = simulatedGoalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING);
		List<GoalAchieverInfo> allSimulateGoals = Strategy.filterTaskGoals(itemSimulatedGoals,
				simulatorManager.getCharacterServiceSimulator(), simulatorManager.getBankDAOSimulator());

		MonsterTaskUseSimulatorFactory factoryMonster = new MonsterTaskUseSimulatorFactory(
				monsterGoals.stream()
						.collect(Collectors.toMap(MonsterGoalAchiever::getMonsterCode, Function.identity())),
				allSimulateGoals.stream()
						.filter(aga -> BotCraftSkill.COOKING.equals(aga.getBotCraftSkill())
								|| BotCraftSkill.ALCHEMY.equals(aga.getBotCraftSkill()))
						.collect(Collectors.toMap(GoalAchieverInfo::getItemCode, GoalAchieverInfo::getGoal)),
				simulatorManager.getBankDAOSimulator(), simulatorManager.getCharacterDAOSimulator(),
				simulatorManager.getGoalFactoryCreator(), simulatorManager.getCharacterServiceSimulator(),
				secondSimulatorManager, secondSimulatorListener, secondSimulatedGoalFactory, 25);

		GoalAchiever simLoopGoal = factoryMonster.createTaskGoalAchiever("pig", 100);
		List<? extends BotItemReader> viewItems = simulatorManager.getBankDAOSimulator().viewItems();
		simulatorManager.setValue(character, viewItems);
		SumAccumulator accumulator = new SumAccumulator();
		simulatorListener.setInnerListener((className, methodName, cooldown, error) -> {
			accumulator.accumulate(cooldown);
			if (error) {
				System.out.println(methodName);
			}
		});
		if (simLoopGoal.isRealisableAfterSetRoot(character)) {
			simLoopGoal.clear();
			boolean result = simLoopGoal.execute(new HashMap<>());
			System.out.println("time :" + accumulator.get() + " : " + result);
		}
	}

	private static void simulateFight(StrategySimulatorListener simulatorListener,
			SimulatorManagerImpl simulatorManager, BotCharacter character, GoalFactory simulatedGoalFactory) {
		List<MonsterGoalAchiever> monsterGoals = simulatedGoalFactory
				.createMonstersGoals(resp -> !resp.fight().isWin());
		SumAccumulator accumulator = new SumAccumulator();
		simulatorListener
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));
		long begin = System.currentTimeMillis();
		List<? extends BotItemReader> viewItems = simulatorManager.getBankDAOSimulator().viewItems();
		for (MonsterGoalAchiever simGoal : monsterGoals) {
			simulatorManager.setValue(character, viewItems);
			accumulator.reset();
			GoalAchieverLoop simLoopGoal = new GoalAchieverLoop(simGoal, 100, false);
			if (simLoopGoal.isRealisableAfterSetRoot(character)) {
				simLoopGoal.clear();
				boolean result = simLoopGoal.execute(new HashMap<>());
				System.out.println(simGoal.getMonsterCode() + ":" + accumulator.get() + ":" + result);
				System.out.println("Potion : "
						+ simulatorManager.getCharacterDAOSimulator().getCharacter().getUtility1SlotQuantity());
				System.out.println("Potion : "
						+ simulatorManager.getCharacterDAOSimulator().getCharacter().getUtility2SlotQuantity());
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Duree:" + (end - begin));
	}

	private static void simulateCrafting(StrategySimulatorListener simulatorListener,
			SimulatorManagerImpl simulatorManager, BotCharacter character, GoalFactory simulatedGoalFactory) {
		Collection<GoalAchieverInfo> itemSimulatedGoals = simulatedGoalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING);
		List<GoalAchieverInfo> allSimulateGoals = Strategy.filterTaskGoals(itemSimulatedGoals,
				simulatorManager.getCharacterServiceSimulator(), simulatorManager.getBankDAOSimulator());

		long begin = System.currentTimeMillis();
		BotCraftSkill craftSkill = BotCraftSkill.GEARCRAFTING;
		List<BotItem> viewItems = Collections.emptyList();
		testStrategy(simulatorListener, simulatorManager, character, viewItems, simulatedGoalFactory, allSimulateGoals,
				craftSkill);
		craftSkill = BotCraftSkill.WEAPONCRAFTING;
		testStrategy(simulatorListener, simulatorManager, character, viewItems, simulatedGoalFactory, allSimulateGoals,
				craftSkill);
		craftSkill = BotCraftSkill.JEWELRYCRAFTING;
		testStrategy(simulatorListener, simulatorManager, character, viewItems, simulatedGoalFactory, allSimulateGoals,
				craftSkill);
		long end = System.currentTimeMillis();
		System.out.println("Duree:" + (end - begin));
	}

	private static void testStrategy(StrategySimulatorListener simulatorListener, SimulatorManagerImpl simulatorManager,
			BotCharacter character, List<BotItem> viewItems, GoalFactory simulatedGoalFactory,
			List<GoalAchieverInfo> allSimulateGoals, BotCraftSkill craftSkill) {
		Bornes bornes = new Bornes(1, 1, 41);
		Predicate<GoalAchieverInfo> simulatedPredicate = StrategySkillUtils.createFilterCraftPredicate(craftSkill,
				bornes);
		List<GoalAchieverInfo> simGoals = allSimulateGoals.stream().filter(simulatedPredicate).toList();
		SumAccumulator accumulator = new SumAccumulator();
		simulatorListener
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));
		GoalAverageOptimizer goalAverageOptimizer = new GoalAverageOptimizerImpl(
				simulatorManager.getCharacterDAOSimulator());
		for (GoalAchieverInfo simGoal : simGoals) {
			boolean success = true;
			accumulator.reset();
			goalAverageOptimizer.optimize(simGoal.getGoal(), 5, 0.9f);
			try {
				for (int i = 0; i < 100; i++) {
					simulatorManager.setValue(character, viewItems);
					if (simGoal.getGoal().isRealisableAfterSetRoot(character)) {
						simGoal.getGoal().clear();
						if (!simGoal.getGoal().execute(new HashMap<>())) {
							success = false;
							break;
						}
					} else {
						success = false;
						// System.out.println("NONREALISABLE : " +
						// simulatedGoalFactory.getInfos(simGoal).getItemCode());
						break;
					}
				}
				if (success) {
					int time = accumulator.get();
					String goalCode = simGoal.getItemCode();
					System.out.println(goalCode + ":" + time);
				}
			} catch (StopSimulationException sse) {
				// Arrêt de la simulation
				sse.printStackTrace();
			}
		}
	}
}
