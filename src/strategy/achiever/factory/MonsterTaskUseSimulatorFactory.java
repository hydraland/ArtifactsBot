package strategy.achiever.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.dao.simulate.StopSimulationException;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotItemReader;
import strategy.SumAccumulator;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.GoalFactory.GoalFilter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.ForceExecuteGoalAchiever;
import strategy.achiever.factory.goals.GenericGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.GoalAverageOptimizerImpl;
import strategy.util.Bornes;
import strategy.util.CharacterService;
import strategy.util.StrategySkillUtils;
import util.Combinator;

public class MonsterTaskUseSimulatorFactory implements MonsterTaskFactory {

	private static final int MAX_SIMULATION_TIME_IN_SECOND = 86400;
	private final Map<String, MonsterGoalAchiever> monsterGoals;
	private final Map<String, MonsterGoalAchiever> simulatedmonstersGoals;
	private final BankDAO bankDAO;
	private final CharacterService characterService;
	private final CharacterDAO characterDAO;
	private final SimulatorManager simulatorManager;
	private final Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals;
	private final Collection<GoalAchieverInfo> cookAndAlchemySimulateGoals;
	private final GenericGoalAchiever genericGoalAchiever;
	private ArtifactGoalAchiever simGoalAchiever;
	private final GoalAverageOptimizer goalAverageOptimizer;
	private final float maxCookOrPotionTaskPercent;
	private final GoalAchiever depositNoReservedItemGoalAchiever;
	private final GoalAverageOptimizer simulateGoalAverageOptimizer;
	private final GoalFactoryCreator factoryCreator;

	public MonsterTaskUseSimulatorFactory(Map<String, MonsterGoalAchiever> monsterGoals,
			Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals, BankDAO bankDAO, CharacterDAO characterDAO,
			GoalFactoryCreator factoryCreator, CharacterService characterService, SimulatorManager simulatorManager,
			GoalFactory simulatedGoalFactory, float maxCookOrPotionTaskPercent) {
		this.factoryCreator = factoryCreator;
		this.goalAverageOptimizer = new GoalAverageOptimizerImpl(characterDAO);
		this.simulateGoalAverageOptimizer = new GoalAverageOptimizerImpl(simulatorManager.getCharacterDAOSimulator());
		this.maxCookOrPotionTaskPercent = maxCookOrPotionTaskPercent;
		this.cookAndAlchemyGoals = cookAndAlchemyGoals;
		this.simulatorManager = simulatorManager;
		this.monsterGoals = monsterGoals;
		this.bankDAO = bankDAO;
		this.characterDAO = characterDAO;
		this.characterService = characterService;
		genericGoalAchiever = factoryCreator.createGenericGoalAchiever();
		simulatedmonstersGoals = simulatedGoalFactory.createMonstersGoals(resp -> !resp.fight().isWin(), GoalFilter.ALL)
				.stream().collect(Collectors.toMap(MonsterGoalAchiever::getMonsterCode, Function.identity()));
		cookAndAlchemySimulateGoals = simulatedGoalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING,
				GoalFilter.ALL);
		depositNoReservedItemGoalAchiever = factoryCreator.createDepositNoReservedItemGoalAchiever();
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total) {
		if (monsterGoals.containsKey(code)) {
			BotCharacter botCharacter = characterDAO.getCharacter();
			List<GoalAchieverInfo> testGoals = initSimulation(code, total, botCharacter);

			String[] simCodeFound = simulate(testGoals, botCharacter, bankDAO.viewItems());
			GoalAchiever subGoal = monsterGoals.get(code);
			if (simCodeFound.length == 1) {
				subGoal = factoryCreator.createGoalAchieverTwoStep(genericGoalAchiever, subGoal, true, true);
				genericGoalAchiever
						.setCheckRealisableGoalAchiever(c -> !characterService.isPossessOnSelf(simCodeFound[0]));
				ArtifactGoalAchiever artifactGoalAchiever = cookAndAlchemyGoals.get(simCodeFound[0]);
				int maxCookOrPotionTask = Math.round(maxCookOrPotionTaskPercent * botCharacter.getInventoryMaxItems());
				goalAverageOptimizer.optimize(artifactGoalAchiever, maxCookOrPotionTask, 0.9f);
				genericGoalAchiever.setExecutableGoalAchiever(reservedItems -> {
					artifactGoalAchiever.clear();
					boolean result = artifactGoalAchiever.execute(reservedItems);
					reservedItems.clear();
					return result;
				});
			} else if (simCodeFound.length > 1) {
				int maxCookOrPotionTask = Math
						.round(maxCookOrPotionTaskPercent * botCharacter.getInventoryMaxItems() / 3);
				updateGenericGoal(simCodeFound, characterService, goalAverageOptimizer, maxCookOrPotionTask);
				subGoal = factoryCreator.createGoalAchieverTwoStep(genericGoalAchiever,
						new ForceExecuteGoalAchiever(subGoal), true, true);
			}

			GoalAchiever goalAchiever = factoryCreator.createGoalAchieverTwoStep(depositNoReservedItemGoalAchiever,
					subGoal, true, true);

			return factoryCreator.createGoalAchieverLoop(goalAchiever, total, false);
		}
		return null;
	}

	private List<GoalAchieverInfo> initSimulation(String code, int total, BotCharacter botCharacter) {
		GoalAchiever simDepositNoReservedItemGoalAchiever = simulatorManager.getGoalFactoryCreator()
				.createDepositNoReservedItemGoalAchiever();
		genericGoalAchiever.setCheckRealisableGoalAchiever(character -> false);
		genericGoalAchiever.setExecutableGoalAchiever(reservedItems -> false);
		GoalAchiever goalAchieverTwoStep = factoryCreator.createGoalAchieverTwoStep(genericGoalAchiever,
				simulatedmonstersGoals.get(code), true, true);
		GoalAchiever goalAchiever = factoryCreator.createGoalAchieverTwoStep(simDepositNoReservedItemGoalAchiever,
				goalAchieverTwoStep, true, true);

		simGoalAchiever = factoryCreator.createGoalAchieverLoop(goalAchiever, total, false);

		Bornes bornes = new Bornes(1, 1, Math.min(botCharacter.getLevel(), botCharacter.getCookingLevel()));
		Predicate<GoalAchieverInfo> simulatedPredicateCook = StrategySkillUtils
				.createFilterCraftPredicate(BotCraftSkill.COOKING, bornes);

		bornes = new Bornes(1, 1, Math.min(botCharacter.getLevel(), botCharacter.getAlchemyLevel()));
		Predicate<GoalAchieverInfo> simulatedPredicatePotion = StrategySkillUtils
				.createFilterCraftPredicate(BotCraftSkill.ALCHEMY, bornes);

		List<GoalAchieverInfo> resultGoals = new ArrayList<>();
		resultGoals.addAll(cookAndAlchemySimulateGoals.stream().filter(simulatedPredicateCook).toList());
		resultGoals.addAll(cookAndAlchemySimulateGoals.stream().filter(simulatedPredicatePotion).toList());
		return resultGoals;
	}

	private String[] simulate(List<GoalAchieverInfo> testGoals, BotCharacter botCharacter,
			List<? extends BotItemReader> botItems) {
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

				for (GoalAchieverInfo artifactGoalAchiever : testGoals) {
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
			}
		} else {
			int minTime = MAX_SIMULATION_TIME_IN_SECOND;
			Combinator<GoalAchieverInfo> combinator = new Combinator<>(GoalAchieverInfo.class, 3);
			List<GoalAchieverInfo> cookingGoal = testGoals.stream()
					.filter(aga -> BotCraftSkill.COOKING.equals(aga.getBotCraftSkill())).toList();
			List<GoalAchieverInfo> potionGoal = testGoals.stream()
					.filter(aga -> BotCraftSkill.ALCHEMY.equals(aga.getBotCraftSkill())).toList();
			List<GoalAchieverInfo> potionGoal2 = new ArrayList<>(potionGoal);
			combinator.set(0, cookingGoal);
			combinator.set(1, potionGoal);
			combinator.set(2, potionGoal2);

			for (GoalAchieverInfo[] artifactGA : combinator) {
				if (artifactGA[1] == artifactGA[2]) {
					continue;
				}

				simulatorManager.setValue(botCharacter, botItems);
				accumulator.setMax(Integer.MAX_VALUE); // Pour ne pas planter dans l'optimisation
				int maxCookOrPotionTask = Math
						.round(maxCookOrPotionTaskPercent * botCharacter.getInventoryMaxItems() / 3);
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
							foundGoalCode = new String[] { artifactGA[0].getItemCode(), artifactGA[1].getItemCode(),
									artifactGA[2].getItemCode() };
						}
					}
				} catch (StopSimulationException sse) {
					// On ne fait rien c'est normal
				}
			}
		}
		return foundGoalCode;
	}

	private GoalAchiever createGoals(GoalAchieverInfo[] artifactGA, CharacterService aCharacterService,
			GoalAverageOptimizer aGoalAverageOptimizer, int optimizeValue) {
		genericGoalAchiever.setCheckRealisableGoalAchiever(character -> Arrays.stream(artifactGA)
				.<Boolean>map(aga -> !aCharacterService.isPossessOnSelf(aga.getItemCode()))
				.reduce(false, (v1, v2) -> v1 || v2));
		Arrays.stream(artifactGA).forEach(aga -> aGoalAverageOptimizer.optimize(aga.getGoal(), optimizeValue, 0.9f));
		genericGoalAchiever.setExecutableGoalAchiever(ri -> {
			boolean resultExec = Arrays.stream(artifactGA).filter(aga -> {
				aga.getGoal().clear();
				return true;
			}).map(aga -> !aCharacterService.isPossessOnSelf(aga.getItemCode()) && aga.getGoal().execute(ri))
					.reduce(false, (v1, v2) -> v1 || v2);
			ri.clear();
			return resultExec;
		});
		return new ForceExecuteGoalAchiever(simGoalAchiever);
	}

	private void updateGenericGoal(String[] simCodeFound, CharacterService aCharacterService,
			GoalAverageOptimizer aGoalAverageOptimizer, int optimizeValue) {
		genericGoalAchiever.setCheckRealisableGoalAchiever(character -> Arrays.stream(simCodeFound)
				.<Boolean>map(code -> !aCharacterService.isPossessOnSelf(code)).reduce(false, (v1, v2) -> v1 || v2));
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
	}
}
