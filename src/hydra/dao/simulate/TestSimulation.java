package hydra.dao.simulate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemReader;
import hydra.model.BotItemType;
import hydra.model.BotMonster;
import strategy.Strategy;
import strategy.SumAccumulator;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.GoalFactory.GoalFilter;
import strategy.achiever.factory.MonsterItemDropUseSimulatorFactory;
import strategy.achiever.factory.MonsterTaskUseSimulatorFactory;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.goals.MonsterItemDropGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.GoalAverageOptimizerImpl;
import strategy.util.Bornes;
import strategy.util.BotItemInfo;
import strategy.util.OptimizeResult;
import strategy.util.StrategySkillUtils;
import strategy.util.fight.factory.DefaultHPRecoveryFactory;
import strategy.util.fight.factory.HPRecoveryUseSimulatorFactory;
import util.JsonToStringStyle;

public final class TestSimulation {
	public static void main(String[] args) throws FileNotFoundException {
		LogManager.getLogManager().getLogger("").setLevel(Level.SEVERE);
		ToStringBuilder.setDefaultStyle(new JsonToStringStyle());
		SimulatorManagerImpl simulatorManager = new SimulatorManagerImpl(botEvents -> new ArrayList<>());
		simulatorManager.load(true);

		BotCharacter character = simulatorManager.getCharacterDAOSimulator().getCharacter();
		System.setOut(new PrintStream(new File("test.txt")));
		GoalParameter goalParameter = new GoalParameter(6, 100, 14, 15);
		GoalFactory simulatedGoalFactory = simulatorManager.createFactory(goalParameter);
		goalParameter.setHPRecoveryFactory(new DefaultHPRecoveryFactory(simulatorManager.getCharacterDAOSimulator(),
				simulatorManager.getItemDAOSimulator(), simulatorManager.getCharacterServiceSimulator()));
		List<BotItemReader> viewItems = new ArrayList<>(simulatorManager.getBankDAOSimulator().viewItems());

		long begin = System.currentTimeMillis();
		simulateCrafting(simulatorManager, character, simulatedGoalFactory, viewItems);
		long inter1 = System.currentTimeMillis();
		System.out.println("Duree Crafting:" + (inter1 - begin));
		//viewItems = viewItems.stream().filter(bir -> !bir.getCode().equals("minor_health_potion")).toList();
		//character.setWeaponSlot("copper_dagger");
		/*character.setLevel(40);
		character.setAlchemyLevel(40);*/
		simulateCookingAndFight(simulatorManager, character, simulatedGoalFactory, goalParameter, viewItems);
		long inter2 = System.currentTimeMillis();
		System.out.println("Duree Cook and Fight:" + (inter2 - inter1));
		simulateDropItem(simulatorManager, character, simulatedGoalFactory, goalParameter, viewItems);
		long inter3 = System.currentTimeMillis();
		System.out.println("Duree Drop:" + (inter3 - inter2));
		simulateFight(simulatorManager, character, simulatedGoalFactory, viewItems);
		long inter4 = System.currentTimeMillis();
		System.out.println("Duree fight:" + (inter4 - inter3));
		List<BotItemDetails> items = simulatorManager.getItemDAOSimulator().getItems();
		//viewItems.addAll(items.stream().filter(bid -> (bid.getLevel() <= i || bid.getCode().equals("snowman_hat") || bid.getCode().equals("bandit_armor") || bid.getCode().equals("death_knight_sword") /*|| bid.getCode().equals("life_crystal")|| bid.getCode().equals("lich_crown")*/) /*&& !bid.getType().equals(BotItemType.UTILITY)*/ && !bid.getType().equals(BotItemType.CONSUMABLE)).map(bid -> {BotItem item = new BotItem(); item.setCode(bid.getCode());item.setQuantity(100);return item;}).toList());
		//viewItems.addAll(items.stream().filter(bid -> !bid.getType().equals(BotItemType.UTILITY) && !bid.getType().equals(BotItemType.CONSUMABLE)).map(bid -> {BotItem item = new BotItem(); item.setCode(bid.getCode());item.setQuantity(100);return item;}).toList());
		character.setLevel(40);
		resetCharacter(character);
		for(int i : new int[] {1,5,10,15,20,25,30,35,40}) {
			System.out.println("*********************************"+i+"*********************************");
			viewItems = new LinkedList<>();
			viewItems.addAll(items.stream().filter(bid -> (bid.getLevel() <= i && !bid.getType().equals(BotItemType.CONSUMABLE))).map(bid -> {BotItem item = new BotItem(); item.setCode(bid.getCode());item.setQuantity(100);return item;}).toList());
			//viewItems.addAll(items.stream().filter(bid -> ((bid.getLevel() <= i && !bid.getType().equals(BotItemType.CONSUMABLE) && !bid.getType().equals(BotItemType.WEAPON)) || (bid.getLevel() <= i+5 && bid.getType().equals(BotItemType.WEAPON)))).map(bid -> {BotItem item = new BotItem(); item.setCode(bid.getCode());item.setQuantity(100);return item;}).toList());
			simulatorManager.setValue(character, viewItems);
			for(BotMonster monster : simulatorManager.getMonsterDAOSimulator().getMonsters()) {
			//BotMonster monster = simulatorManager.getMonsterDAOSimulator().getMonster("mushmush");
				System.out.println(monster.getCode());
				OptimizeResult optimizeEquipementsPossesed = simulatorManager.getFightService().optimizeEquipementsPossesed(monster, Collections.emptyMap(), false);
				if(optimizeEquipementsPossesed.fightDetails().win())
					System.out.println(optimizeEquipementsPossesed);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Duree:" + (end - begin));
	}

	private static void resetCharacter(BotCharacter character) {
		character.setUtility1Slot("");
		character.setUtility2Slot("");
		character.setWeaponSlot("");
		character.setBodyArmorSlot("");
		character.setBootsSlot("");
		character.setHelmetSlot("");
		character.setShieldSlot("");
		character.setLegArmorSlot("");
		character.setAmuletSlot("");
		character.setRing1Slot("");
		character.setRing2Slot("");
		character.setArtifact1Slot("");
		character.setArtifact2Slot("");
		character.setArtifact3Slot("");
		character.setAttackAir(0); 
		character.setAttackEarth(0); 
		character.setAttackFire(0); 
		character.setAttackWater(0); 
		character.setResAir(0); 
		character.setResEarth(0); 
		character.setResFire(0); 
		character.setResWater(0); 
		character.setHp(320); 
		character.setDmgAir(0); 
		character.setDmgEarth(0); 
		character.setDmgFire(0); 
		character.setDmgWater(0); 
		character.setUtility1SlotQuantity(0); 
		character.setUtility2SlotQuantity(0); 
		character.setMaxHp(320); 
		character.setHaste(0); 
	}

	private static void simulateDropItem(SimulatorManagerImpl simulatorManager, BotCharacter character,
			GoalFactory simulatedGoalFactory, GoalParameter goalParameter, List<BotItemReader> viewItems) {

		SimulatorManager secondSimulatorManager = new SimulatorManagerImpl(botEvents -> new ArrayList<>());
		secondSimulatorManager.load(true);

		goalParameter.setHPRecoveryFactory(new HPRecoveryUseSimulatorFactory(
				simulatorManager.getCharacterDAOSimulator(), simulatorManager.getItemDAOSimulator(),
				simulatorManager.getBankDAOSimulator(), simulatorManager.getMoveService(),
				simulatorManager.getCharacterServiceSimulator(), secondSimulatorManager));

		GoalParameter simulateGoalParameter = new GoalParameter(goalParameter.getMinFreeSlot(),
				goalParameter.getRareItemSeuil(), goalParameter.getCoinReserve(),
				goalParameter.getMinFreeInventorySpace());
		simulateGoalParameter.setHPRecoveryFactory(new DefaultHPRecoveryFactory(
				secondSimulatorManager.getCharacterDAOSimulator(), secondSimulatorManager.getItemDAOSimulator(),
				secondSimulatorManager.getCharacterServiceSimulator()));
		GoalFactory secondSimulatedGoalFactory = secondSimulatorManager.createFactory(simulateGoalParameter);

		Collection<GoalAchieverInfo<ArtifactGoalAchiever>> itemSimulatedGoals = simulatedGoalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING, GoalFilter.ALL);
		List<GoalAchieverInfo<ArtifactGoalAchiever>> allSimulateGoals = Strategy.filterTaskGoals(itemSimulatedGoals,
				simulatorManager.getCharacterServiceSimulator(), simulatorManager.getBankDAOSimulator());

		MonsterItemDropUseSimulatorFactory factoryMonster = new MonsterItemDropUseSimulatorFactory(
				allSimulateGoals.stream()
						.filter(aga -> BotCraftSkill.COOKING.equals(aga.getBotCraftSkill())
								|| BotCraftSkill.ALCHEMY.equals(aga.getBotCraftSkill()))
						.collect(Collectors.toMap(GoalAchieverInfo::getItemCode, GoalAchieverInfo::getGoal)),
				simulatorManager.getBankDAOSimulator(), simulatorManager.getCharacterDAOSimulator(),
				simulatorManager.getItemDAOSimulator(), simulatorManager.getGoalFactoryCreator(),
				simulatorManager.getCharacterServiceSimulator(), secondSimulatorManager, secondSimulatedGoalFactory,
				0.3f, simulateGoalParameter, goalParameter);
		goalParameter.setMonsterItemDropFactory(factoryMonster);
		// goalParameter.setMonsterItemDropFactory(new
		// DefaultMonsterItemDropFactory(simulatorManager.getGoalFactoryCreator()));

		List<GoalAchieverInfo<MonsterItemDropGoalAchiever>> dropItemGoal = simulatedGoalFactory.createDropItemGoal();
		SumAccumulator accumulator = new SumAccumulator();
		simulatorManager.getSimulatorListener().setInnerListener((className, methodName, cooldown, error) -> {
			accumulator.accumulate(cooldown);
			if (error) {
				System.out.println(methodName);
			}
		});
		for (GoalAchieverInfo<MonsterItemDropGoalAchiever> simLoopGoal : dropItemGoal) {
			System.out.println("Monster :" + simLoopGoal.getMonsterCode());
			simulatorManager.setValue(character, viewItems);
			if (simLoopGoal.getGoal().isRealisableAfterSetRoot(character)) {
				simLoopGoal.getGoal().clear();
				boolean result = simLoopGoal.getGoal().execute(new HashMap<>());
				System.out.println("time :" + accumulator.get() + " : " + result);
			}
			accumulator.reset();
		}
	}

	private static void simulateCookingAndFight(SimulatorManagerImpl simulatorManager, BotCharacter character,
			GoalFactory simulatedGoalFactory, GoalParameter goalParameter, List<BotItemReader> viewItems) {

		SimulatorManager secondSimulatorManager = new SimulatorManagerImpl(botEvents -> new ArrayList<>());
		secondSimulatorManager.load(true);
		secondSimulatorManager.setValue(character, viewItems);
		simulatorManager.setValue(character, viewItems);

		goalParameter.setHPRecoveryFactory(new HPRecoveryUseSimulatorFactory(
				simulatorManager.getCharacterDAOSimulator(), simulatorManager.getItemDAOSimulator(),
				simulatorManager.getBankDAOSimulator(), simulatorManager.getMoveService(),
				simulatorManager.getCharacterServiceSimulator(), secondSimulatorManager));

		GoalParameter simulateGoalParameter = new GoalParameter(goalParameter.getMinFreeSlot(),
				goalParameter.getRareItemSeuil(), goalParameter.getCoinReserve(),
				goalParameter.getMinFreeInventorySpace());
		simulateGoalParameter.setHPRecoveryFactory(new DefaultHPRecoveryFactory(
				secondSimulatorManager.getCharacterDAOSimulator(), secondSimulatorManager.getItemDAOSimulator(),
				secondSimulatorManager.getCharacterServiceSimulator()));
		GoalFactory secondSimulatedGoalFactory = secondSimulatorManager.createFactory(simulateGoalParameter);

		List<MonsterGoalAchiever> monsterGoals = simulatedGoalFactory.createMonstersGoals(resp -> !resp.fight().isWin(),
				GoalFilter.ALL);

		Collection<GoalAchieverInfo<ArtifactGoalAchiever>> itemSimulatedGoals = simulatedGoalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING, GoalFilter.ALL);
		List<GoalAchieverInfo<ArtifactGoalAchiever>> allSimulateGoals = Strategy.filterTaskGoals(itemSimulatedGoals,
				simulatorManager.getCharacterServiceSimulator(), simulatorManager.getBankDAOSimulator());

		MonsterTaskUseSimulatorFactory factoryMonster = new MonsterTaskUseSimulatorFactory(
				monsterGoals.stream()
						.collect(Collectors.toMap(MonsterGoalAchiever::getMonsterCode, Function.identity())),
				allSimulateGoals.stream()
						.filter(aga -> BotCraftSkill.COOKING.equals(aga.getBotCraftSkill())
								|| BotCraftSkill.ALCHEMY.equals(aga.getBotCraftSkill()))
						.collect(Collectors.toMap(GoalAchieverInfo::getItemCode, GoalAchieverInfo::getGoal)),
				simulatorManager.getBankDAOSimulator(), simulatorManager.getCharacterDAOSimulator(),
				simulatorManager.getItemDAOSimulator(), simulatorManager.getGoalFactoryCreator(),
				simulatorManager.getCharacterServiceSimulator(), secondSimulatorManager, secondSimulatedGoalFactory,
				0.3f,simulateGoalParameter,goalParameter);

		long beginSimu = System.currentTimeMillis();
		GoalAchiever simLoopGoal = factoryMonster.createTaskGoalAchiever("vampire", 100);
		long endSimu = System.currentTimeMillis();
		System.out.println("Durée simu : "+(endSimu-beginSimu));
		simulatorManager.setValue(character, viewItems);
		SumAccumulator accumulator = new SumAccumulator();
		simulatorManager.getSimulatorListener().setInnerListener((className, methodName, cooldown, error) -> {
			accumulator.accumulate(cooldown);
			if (error) {
				System.out.println(methodName);
			}
		});
		if (simLoopGoal.isRealisableAfterSetRoot(character)) {
			simLoopGoal.clear();
			System.out.println("Execute cyclops task");
			boolean result = simLoopGoal.execute(new HashMap<>());
			System.out.println("time :" + accumulator.get() + " : " + result);
		}
	}

	private static void simulateFight(SimulatorManagerImpl simulatorManager, BotCharacter character,
			GoalFactory simulatedGoalFactory, List<BotItemReader> viewItems) {
		List<MonsterGoalAchiever> monsterGoals = simulatedGoalFactory.createMonstersGoals(resp -> !resp.fight().isWin(),
				GoalFilter.ALL);
		SumAccumulator accumulator = new SumAccumulator();
		simulatorManager.getSimulatorListener()
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));
		for (MonsterGoalAchiever simGoal : monsterGoals) {
			simulatorManager.setValue(character, viewItems);
			accumulator.reset();
			GoalAchieverLoop simLoopGoal = new GoalAchieverLoop(simGoal, 100, false);
			if (simLoopGoal.isRealisableAfterSetRoot(character)) {
				simLoopGoal.clear();
				boolean result = simLoopGoal.execute(new HashMap<>());
				System.out.println(simGoal.getMonsterCode() + ":" + accumulator.get() + ":" + result);
			}
		}
	}

	private static void simulateCrafting(SimulatorManagerImpl simulatorManager, BotCharacter character,
			GoalFactory simulatedGoalFactory, List<BotItemReader> viewItems) {
		Collection<GoalAchieverInfo<ArtifactGoalAchiever>> itemSimulatedGoals = simulatedGoalFactory
				.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING, GoalFilter.ALL);
		List<GoalAchieverInfo<ArtifactGoalAchiever>> allSimulateGoals = Strategy.filterTaskGoals(itemSimulatedGoals,
				simulatorManager.getCharacterServiceSimulator(), simulatorManager.getBankDAOSimulator());

		BotCraftSkill craftSkill = BotCraftSkill.GEARCRAFTING;
		testStrategy(simulatorManager, character, viewItems, simulatedGoalFactory, allSimulateGoals, craftSkill);
		craftSkill = BotCraftSkill.WEAPONCRAFTING;
		testStrategy(simulatorManager, character, viewItems, simulatedGoalFactory, allSimulateGoals, craftSkill);
		craftSkill = BotCraftSkill.JEWELRYCRAFTING;
		testStrategy(simulatorManager, character, viewItems, simulatedGoalFactory, allSimulateGoals, craftSkill);
	}

	private static void testStrategy(SimulatorManagerImpl simulatorManager, BotCharacter character,
			List<BotItemReader> viewItems, GoalFactory simulatedGoalFactory,
			List<GoalAchieverInfo<ArtifactGoalAchiever>> allSimulateGoals, BotCraftSkill craftSkill) {
		Bornes bornes = new Bornes(1, 1, 41);
		Predicate<GoalAchieverInfo<ArtifactGoalAchiever>> simulatedPredicate = StrategySkillUtils
				.createFilterCraftPredicate(craftSkill, bornes);
		List<GoalAchieverInfo<ArtifactGoalAchiever>> simGoals = allSimulateGoals.stream().filter(simulatedPredicate)
				.toList();
		SumAccumulator accumulator = new SumAccumulator();
		simulatorManager.getSimulatorListener()
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));
		GoalAverageOptimizer goalAverageOptimizer = new GoalAverageOptimizerImpl(
				simulatorManager.getCharacterDAOSimulator());
		for (GoalAchieverInfo<ArtifactGoalAchiever> simGoal : simGoals) {
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
						//System.out.println("NONREALISABLE : " + simGoal.getItemCode());
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
