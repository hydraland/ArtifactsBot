package hydra.dao.simulate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotItem;
import strategy.HPRecoveryUseSimulator;
import strategy.Strategy;
import strategy.StrategySimulatorListener;
import strategy.SumAccumulator;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.ArtifactGoalAchiever;
import strategy.achiever.factory.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.GoalAchieverList;
import strategy.achiever.factory.GoalAchieverLoop;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.MonsterGoalAchiever;
import strategy.util.Bornes;
import strategy.util.StrategySkillUtils;
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
		GoalFactory simulatedGoalFactory = simulatorManager.createFactory(new GoalParameter(6, 100, 14, 15, false));
		simulateCrafting(simulatorListener, simulatorManager, character,
		simulatedGoalFactory);
		simulateFight(simulatorListener, simulatorManager, character,
		simulatedGoalFactory);
		simulateCookingAndFight(simulatorListener, simulatorManager, character, simulatedGoalFactory);
	}

	private static void simulateCookingAndFight(StrategySimulatorListener simulatorListener,
			SimulatorManagerImpl simulatorManager, BotCharacter character, GoalFactory simulatedGoalFactory) {
		simulatorManager.getFightService()
				.sethPRecovery(new HPRecoveryUseSimulator(simulatorManager.getCharacterDAOSimulator(),
						simulatorManager.getItemDAOSimulator(), simulatorManager.getCharacterServiceSimulator()));
		List<MonsterGoalAchiever> monsterGoals = simulatedGoalFactory.createMonstersGoals(resp -> !resp.fight().isWin());
		SumAccumulator accumulator = new SumAccumulator();
		simulatorListener
				.setInnerListener((className, methodName, cooldown, error) -> {accumulator.accumulate(cooldown); if(error) {System.out.println(methodName);}});
		MonsterGoalAchiever monsterGoal = monsterGoals.stream().filter(mga -> mga.getMonsterCode().equals("wolf"))
				.findFirst().get();
		GoalAchieverLoop simLoopGoal = new GoalAchieverLoop(simulatedGoalFactory.addDepositNoReservedItemGoalAchiever(monsterGoal), 100, false);
		Bornes bornes = new Bornes(1, 1, 21);
		Predicate<ArtifactGoalAchiever> simulatedPredicate = StrategySkillUtils
				.createFilterCraftPredicate(simulatedGoalFactory, BotCraftSkill.COOKING, bornes);
		List<ArtifactGoalAchiever> itemSimulatedGoals = simulatedGoalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING);
		List<ArtifactGoalAchiever> allSimulateGoals = Strategy.filterTaskGoals(itemSimulatedGoals,
				simulatorManager.getCharacterServiceSimulator(), simulatedGoalFactory,
				simulatorManager.getBankDAOSimulator());
		List<ArtifactGoalAchiever> cookingSimGoals = allSimulateGoals.stream().filter(simulatedPredicate).toList();
		List<BotItem> viewItems = simulatorManager.getBankDAOSimulator().viewItems();
		long begin = System.currentTimeMillis();
		simulatorManager.setValue(character, viewItems);
		if (simLoopGoal.isRealisableAfterSetRoot(character)) {
			simLoopGoal.clear();
			boolean result = simLoopGoal.execute(new HashMap<>());
			System.out.println("time :" + accumulator.get() + " : " + result);
		}
		for (ArtifactGoalAchiever artifactGoalAchiever : cookingSimGoals) {
			simulatorManager.setValue(character, viewItems);
			GoalAchieverList achieverList = new GoalAchieverList();
			GoalAchieverLoop cookingLoop = new GoalAchieverLoop(artifactGoalAchiever, 1, false);
			simulatedGoalFactory.getGoalAverageOptimizer().optimize(cookingLoop, 100, 0.9f);
			achieverList.add(cookingLoop);
			achieverList.add(simLoopGoal);
			if (achieverList.isRealisableAfterSetRoot(character)) {
				achieverList.clear();
				boolean result = achieverList.execute(new HashMap<>());
				System.out.println("time :" + accumulator.get() + " : " + result + ":" + artifactGoalAchiever);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Duree:" + (end - begin));

	}

	private static void simulateFight(StrategySimulatorListener simulatorListener,
			SimulatorManagerImpl simulatorManager, BotCharacter character, GoalFactory simulatedGoalFactory) {
		simulatorManager.getFightService()
				.sethPRecovery(new HPRecoveryUseSimulator(simulatorManager.getCharacterDAOSimulator(),
						simulatorManager.getItemDAOSimulator(), simulatorManager.getCharacterServiceSimulator()));
		List<MonsterGoalAchiever> monsterGoals = simulatedGoalFactory.createMonstersGoals(resp -> !resp.fight().isWin());
		SumAccumulator accumulator = new SumAccumulator();
		simulatorListener
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));
		long begin = System.currentTimeMillis();
		List<BotItem> viewItems = simulatorManager.getBankDAOSimulator().viewItems();
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
		List<ArtifactGoalAchiever> itemSimulatedGoals = simulatedGoalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING);
		List<ArtifactGoalAchiever> allSimulateGoals = Strategy.filterTaskGoals(itemSimulatedGoals,
				simulatorManager.getCharacterServiceSimulator(), simulatedGoalFactory,
				simulatorManager.getBankDAOSimulator());

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
			List<ArtifactGoalAchiever> allSimulateGoals, BotCraftSkill craftSkill) {
		Bornes bornes = new Bornes(1, 1, 41);
		Predicate<ArtifactGoalAchiever> simulatedPredicate = StrategySkillUtils
				.createFilterCraftPredicate(simulatedGoalFactory, craftSkill, bornes);
		List<ArtifactGoalAchiever> simGoals = allSimulateGoals.stream().filter(simulatedPredicate).toList();
		SumAccumulator accumulator = new SumAccumulator();
		simulatorListener
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));

		for (ArtifactGoalAchiever simGoal : simGoals) {
			boolean success = true;
			accumulator.reset();
			simulatedGoalFactory.getGoalAverageOptimizer().optimize(simGoal, 5, 0.9f);
			try {
				for (int i = 0; i < 100; i++) {
					simulatorManager.setValue(character, viewItems);
					if (simGoal.isRealisableAfterSetRoot(character)) {
						simGoal.clear();
						if (!simGoal.execute(new HashMap<>())) {
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
					String goalCode = simulatedGoalFactory.getInfos(simGoal).getItemCode();
					System.out.println(goalCode + ":" + time);
				}
			} catch (StopSimulationException sse) {
				// Arrêt de la simulation
				sse.printStackTrace();
			}
		}
	}
}
