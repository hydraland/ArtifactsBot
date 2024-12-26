package strategy.achiever.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import hydra.dao.ItemDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.dao.simulate.StopSimulationException;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotCraftSkill;
import hydra.model.BotItemReader;
import strategy.SumAccumulator;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.GoalFactory.GoalFilter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.ForceExecuteGoalAchiever;
import strategy.achiever.factory.goals.GenericGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.GoalAverageOptimizerImpl;
import strategy.util.Bornes;
import strategy.util.BotItemInfo;
import strategy.util.CharacterService;
import strategy.util.ItemOrigin;
import strategy.util.OptimizeResult;
import strategy.util.StrategySkillUtils;
import util.Combinator;

public abstract class UseSimulatorFactory {

	protected final SimulatorManager simulatorManager;
	protected GoalAchiever simGoalAchiever;
	protected final GenericGoalAchiever genericGoalAchiever;
	private final Collection<GoalAchieverInfo<ArtifactGoalAchiever>> cookAndAlchemySimulateGoals;
	private final float maxCookOrPotionTaskPercent;
	private final GoalAverageOptimizer simulateGoalAverageOptimizer;
	private final Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals;
	private final ItemDAO itemDAO;

	protected UseSimulatorFactory(SimulatorManager simulatorManager, GoalFactoryCreator factoryCreator,
			GoalFactory simulatedGoalFactory, float maxCookOrPotionTaskPercent,
			Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals, ItemDAO itemDAO) {
		this.simulatorManager = simulatorManager;
		this.maxCookOrPotionTaskPercent = maxCookOrPotionTaskPercent;
		this.cookAndAlchemyGoals = cookAndAlchemyGoals;
		this.itemDAO = itemDAO;
		genericGoalAchiever = factoryCreator.createGenericGoalAchiever();
		cookAndAlchemySimulateGoals = simulatedGoalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING,
				GoalFilter.ALL);
		this.simulateGoalAverageOptimizer = new GoalAverageOptimizerImpl(simulatorManager.getCharacterDAOSimulator());
	}

	protected final List<GoalAchieverInfo<ArtifactGoalAchiever>> initSimulation(BotCharacter botCharacter) {
		genericGoalAchiever.setCheckRealisableGoalAchiever(character -> false);
		genericGoalAchiever.setExecutableGoalAchiever(reservedItems -> false);
		genericGoalAchiever.setValue("");

		Bornes bornes = new Bornes(1, 1, Math.min(botCharacter.getLevel(), botCharacter.getCookingLevel()));
		Predicate<GoalAchieverInfo<ArtifactGoalAchiever>> simulatedPredicateCook = StrategySkillUtils
				.createFilterCraftPredicate(BotCraftSkill.COOKING, bornes);

		bornes = new Bornes(1, 1, Math.min(botCharacter.getLevel(), botCharacter.getAlchemyLevel()));
		Predicate<GoalAchieverInfo<ArtifactGoalAchiever>> simulatedPredicatePotion = StrategySkillUtils
				.createFilterCraftPredicate(BotCraftSkill.ALCHEMY, bornes);

		List<GoalAchieverInfo<ArtifactGoalAchiever>> resultGoals = new ArrayList<>();
		resultGoals.addAll(cookAndAlchemySimulateGoals.stream().filter(simulatedPredicateCook).toList());
		resultGoals.addAll(cookAndAlchemySimulateGoals.stream().filter(simulatedPredicatePotion).toList());
		return resultGoals;
	}

	protected final String[] simulate(List<GoalAchieverInfo<ArtifactGoalAchiever>> testGoals, BotCharacter botCharacter,
			List<? extends BotItemReader> botItems, String monsterCode) {
		SumAccumulator accumulator = new SumAccumulator();
		simulatorManager.getSimulatorListener()
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));

		String[] foundGoalCode = new String[0];
		Map<String, Integer> reservedItems = new HashMap<>();
		simulatorManager.setValue(botCharacter, botItems);
		if (simGoalAchiever.isRealisableAfterSetRoot(simulatorManager.getCharacterDAOSimulator().getCharacter())) {
			simGoalAchiever.clear();
			boolean result = simGoalAchiever.execute(reservedItems);

			if (result) {
				int minTime = accumulator.get();

				for (GoalAchieverInfo<ArtifactGoalAchiever> artifactGoalAchiever : testGoals) {
					simulatorManager.setValue(botCharacter, botItems);
					accumulator.setMax(Integer.MAX_VALUE);// Pour ne pas planter dans l'optimisation
					genericGoalAchiever.setCheckRealisableGoalAchiever(character -> !simulatorManager
							.getCharacterServiceSimulator().isPossessOnSelf(artifactGoalAchiever.getItemCode()));
					ArtifactGoalAchiever goal = artifactGoalAchiever.getGoal();
					int maxCookOrPotionTask = Math
							.round(maxCookOrPotionTaskPercent * botCharacter.getInventoryMaxItems());
					simulateGoalAverageOptimizer.optimize(goal, maxCookOrPotionTask, 0.9f);
					genericGoalAchiever.setExecutableGoalAchiever(ri -> {
						goal.clear();
						boolean resultExec = goal.execute(ri);
						ri.clear();
						return resultExec;
					});
					genericGoalAchiever.setValue(goal);
					accumulator.reset();
					accumulator.setMax(minTime);
					try {
						if (simGoalAchiever
								.isRealisableAfterSetRoot(simulatorManager.getCharacterDAOSimulator().getCharacter())) {
							simGoalAchiever.clear();
							reservedItems.clear();
							if (simGoalAchiever.execute(reservedItems) && accumulator.get() < minTime) {
								minTime = accumulator.get();
								foundGoalCode = new String[] { artifactGoalAchiever.getItemCode() };
							}
						}
					} catch (StopSimulationException sse) {
						// On ne fait rien c'est normal
					}
				}
				return foundGoalCode;
			}
		}
		int minTime = getMaxSimulationTime();
		int maxCookOrPotionTask = Math.round(maxCookOrPotionTaskPercent * botCharacter.getInventoryMaxItems() / 3);
		OptimizeResult optimizeEquipementsPossesed = simulatorManager.getFightService().optimizeEquipementsPossesed(
				simulatorManager.getMonsterDAOSimulator().getMonster(monsterCode), new HashMap<>());
		Map<BotCharacterInventorySlot, List<BotItemInfo>> eqtList = new EnumMap<>(BotCharacterInventorySlot.class);
		List<BotItemInfo> utility1 = testGoals.stream()
				.filter(aga -> BotCraftSkill.ALCHEMY.equals(aga.getBotCraftSkill()))
				.map(aga -> new BotItemInfo(itemDAO.getItem(aga.getItemCode()), maxCookOrPotionTask,
						ItemOrigin.ON_SELF))
				.collect(Collectors.toList());
		List<BotItemInfo> utility2 = new ArrayList<>(utility1);
		BotItemInfo[] bestEqts = optimizeEquipementsPossesed.bestEqt();
		eqtList.put(BotCharacterInventorySlot.WEAPON, newList(bestEqts[0]));
		eqtList.put(BotCharacterInventorySlot.BODY_ARMOR, newList(bestEqts[1]));
		eqtList.put(BotCharacterInventorySlot.BOOTS, newList(bestEqts[2]));
		eqtList.put(BotCharacterInventorySlot.HELMET, newList(bestEqts[3]));
		eqtList.put(BotCharacterInventorySlot.SHIELD, newList(bestEqts[4]));
		eqtList.put(BotCharacterInventorySlot.LEG_ARMOR, newList(bestEqts[5]));
		eqtList.put(BotCharacterInventorySlot.AMULET, newList(bestEqts[6]));
		eqtList.put(BotCharacterInventorySlot.RING1, newList(bestEqts[7]));
		eqtList.put(BotCharacterInventorySlot.RING2, newList(bestEqts[8]));
		eqtList.put(BotCharacterInventorySlot.UTILITY1, utility1);
		eqtList.put(BotCharacterInventorySlot.UTILITY2, utility2);

		eqtList.put(BotCharacterInventorySlot.ARTIFACT1, newList(bestEqts[11]));
		eqtList.put(BotCharacterInventorySlot.ARTIFACT2, newList(bestEqts[12]));
		eqtList.put(BotCharacterInventorySlot.ARTIFACT3, newList(bestEqts[13]));
		OptimizeResult optimizeEquipements = simulatorManager.getFightService().optimizeEquipements(
				simulatorManager.getMonsterDAOSimulator().getMonster(monsterCode), eqtList, true, true);
		if (optimizeEquipements.fightDetails().eval() >= 1) {
			List<GoalAchieverInfo<ArtifactGoalAchiever>> cookingGoal = testGoals.stream()
					.filter(aga -> BotCraftSkill.COOKING.equals(aga.getBotCraftSkill())).toList();
			GoalAchieverInfo<ArtifactGoalAchiever> potionGoal = optimizeEquipements.bestEqt()[9] == null ? null
					: testGoals.stream()
							.filter(aga -> aga.getItemCode()
									.equals(optimizeEquipements.bestEqt()[9].botItemDetails().getCode()))
							.findFirst().get();
			GoalAchieverInfo<ArtifactGoalAchiever> potionGoal2 = optimizeEquipements.bestEqt()[10] == null ? null
					: testGoals.stream()
							.filter(aga -> aga.getItemCode()
									.equals(optimizeEquipements.bestEqt()[10].botItemDetails().getCode()))
							.findFirst().get();
			int nbCombinator = 1 + (potionGoal == null ? 0 : 1) + (potionGoal2 == null ? 0 : 1);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Combinator<GoalAchieverInfo<ArtifactGoalAchiever>> combinator = new Combinator(GoalAchieverInfo.class,
					nbCombinator);
			combinator.set(0, cookingGoal);
			if (nbCombinator == 2) {
				combinator.set(1, potionGoal == null ? Arrays.asList(potionGoal2) : Arrays.asList(potionGoal));
			} else if (nbCombinator == 3) {
				combinator.set(1, Arrays.asList(potionGoal));
				combinator.set(2, Arrays.asList(potionGoal2));
			}

			for (GoalAchieverInfo<ArtifactGoalAchiever>[] artifactGA : combinator) {

				simulatorManager.setValue(botCharacter, botItems);
				accumulator.setMax(Integer.MAX_VALUE); // Pour ne pas planter dans l'optimisation
				GoalAchiever testGoal = createGoals(artifactGA, simulatorManager.getCharacterServiceSimulator(),
						simulateGoalAverageOptimizer, maxCookOrPotionTask);
				accumulator.reset();
				accumulator.setMax(minTime);
				try {
					if (testGoal.isRealisableAfterSetRoot(simulatorManager.getCharacterDAOSimulator().getCharacter())) {
						testGoal.clear();
						reservedItems.clear();
						if (testGoal.execute(reservedItems) && accumulator.get() < minTime) {
							minTime = accumulator.get();
							foundGoalCode = new String[nbCombinator];
							for (int i = 0; i < nbCombinator; i++) {
								foundGoalCode[i] = artifactGA[i].getItemCode();
							}
						}
					}
				} catch (StopSimulationException sse) {
					// On ne fait rien c'est normal
				}
			}
		}
		return foundGoalCode;

	}

	private List<BotItemInfo> newList(BotItemInfo botItemInfo) {
		List<BotItemInfo> returnList = new ArrayList<>();
		returnList.add(botItemInfo);
		return returnList;
	}

	protected abstract int getMaxSimulationTime();

	protected final GoalAchiever createGoals(GoalAchieverInfo<ArtifactGoalAchiever>[] artifactGA,
			CharacterService aCharacterService, GoalAverageOptimizer aGoalAverageOptimizer, int optimizeValue) {
		genericGoalAchiever.setCheckRealisableGoalAchiever(
				character -> Arrays.stream(artifactGA).filter(aga -> aga.getGoal().isRealisable(character))
						.<Boolean>map(aga -> !aCharacterService.isPossessOnSelf(aga.getItemCode()))//TODO a revoir cr�er plutot isPosseInInventory
						.reduce(false, (v1, v2) -> v1 || v2));
		Arrays.stream(artifactGA).forEach(aga -> aGoalAverageOptimizer.optimize(aga.getGoal(), optimizeValue, 0.9f));
		genericGoalAchiever.setExecutableGoalAchiever(ri -> {
			boolean resultExec = Arrays.stream(artifactGA).filter(aga -> {
				aga.getGoal().clear();
				return true;
			}).map(aga -> !aCharacterService.isPossessOnSelf(aga.getItemCode()) && aga.getGoal().execute(ri))//TODO pour le simulateur faire la cr�ation instantan�ment (depositInInventory et check)
					.reduce(false, (v1, v2) -> v1 || v2);
			ri.clear();
			return resultExec;
		});
		genericGoalAchiever.setValue(artifactGA);
		return new ForceExecuteGoalAchiever(simGoalAchiever);
	}

	protected final void updateGenericGoal(String[] simCodeFound, CharacterService aCharacterService,
			GoalAverageOptimizer aGoalAverageOptimizer, int optimizeValue) {
		genericGoalAchiever.setCheckRealisableGoalAchiever(character -> Arrays.stream(simCodeFound)
				.filter(code -> cookAndAlchemyGoals.get(code).isRealisable(character))
				.<Boolean>map(code -> !aCharacterService.isPossessOnSelf(code)).reduce(false, (v1, v2) -> v1 || v2));//TODO a revoir cr�er plutot isPosseInInventory
		Arrays.stream(simCodeFound)
				.forEach(code -> aGoalAverageOptimizer.optimize(cookAndAlchemyGoals.get(code), optimizeValue, 0.9f));
		genericGoalAchiever.setExecutableGoalAchiever(ri -> {
			boolean resultExec = Arrays.stream(simCodeFound).filter(code -> {
				ArtifactGoalAchiever aga = cookAndAlchemyGoals.get(code);
				aga.clear();
				return true;
			}).map(code -> !aCharacterService.isPossessOnSelf(code) && cookAndAlchemyGoals.get(code).execute(ri))
					.reduce(false, (v1, v2) -> v1 || v2);
			ri.clear();
			return resultExec;
		});
		genericGoalAchiever.setValue(simCodeFound);
	}
}
