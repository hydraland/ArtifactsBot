package strategy.achiever.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.dao.simulate.StopSimulationException;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotItemReader;
import strategy.StrategySimulatorListener;
import strategy.SumAccumulator;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.ForceExecuteGoalAchiever;
import strategy.achiever.factory.goals.GenericGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.GoalAchieverTwoStep;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
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
	private final List<ArtifactGoalAchiever> cookAndAlchemySimulateGoals;
	private final GenericGoalAchiever genericGoalAchiever;
	private GoalAchieverLoop simGoalAchiever;
	private final GoalFactory simulatedGoalFactory;
	private final GoalAverageOptimizer goalAverageOptimizer;
	private final StrategySimulatorListener simulatorListener;
	private final int maxCookOrPotionTask;
	private final DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever;
	private final GoalAverageOptimizerImpl simulateGoalAverageOptimizer;
	private final GoalFactoryCreator factoryCreator;

	public MonsterTaskUseSimulatorFactory(Map<String, MonsterGoalAchiever> monsterGoals,
			Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals, BankDAO bankDAO, CharacterDAO characterDAO,
			GoalFactoryCreator factoryCreator, CharacterService characterService, SimulatorManager simulatorManager,
			StrategySimulatorListener simulatorListener, GoalFactory simulatedGoalFactory, int maxCookOrPotionTask) {
		this.factoryCreator = factoryCreator;
		this.goalAverageOptimizer = new GoalAverageOptimizerImpl(characterDAO);
		this.simulateGoalAverageOptimizer = new GoalAverageOptimizerImpl(simulatorManager.getCharacterDAOSimulator());
		this.simulatorListener = simulatorListener;
		this.maxCookOrPotionTask = maxCookOrPotionTask;
		this.cookAndAlchemyGoals = cookAndAlchemyGoals;
		this.simulatorManager = simulatorManager;
		this.monsterGoals = monsterGoals;
		this.bankDAO = bankDAO;
		this.characterDAO = characterDAO;
		this.characterService = characterService;
		genericGoalAchiever = new GenericGoalAchiever(character -> false, reservedItems -> false);
		this.simulatedGoalFactory = simulatedGoalFactory;
		simulatedmonstersGoals = simulatedGoalFactory.createMonstersGoals(resp -> !resp.fight().isWin()).stream()
				.collect(Collectors.toMap(MonsterGoalAchiever::getMonsterCode, Function.identity()));
		cookAndAlchemySimulateGoals = simulatedGoalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING);
		depositNoReservedItemGoalAchiever = factoryCreator.createDepositNoReservedItemGoalAchiever();
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total) {
		if (monsterGoals.containsKey(code)) {
			List<ArtifactGoalAchiever> testGoals = initSimulation(code, total, characterDAO.getCharacter());

			String[] simCodeFound = simulate(testGoals, characterDAO.getCharacter(), bankDAO.viewItems());
			GoalAchiever subGoal = monsterGoals.get(code);
			if (simCodeFound.length == 1) {
				subGoal = new GoalAchieverTwoStep(characterDAO, genericGoalAchiever, subGoal, true, true);
				genericGoalAchiever.setCheckRealisableGoalAchiever(
						character -> !characterService.isPossessOnSelf(simCodeFound[0]));
				ArtifactGoalAchiever artifactGoalAchiever = cookAndAlchemyGoals.get(simCodeFound[0]);
				goalAverageOptimizer.optimize(artifactGoalAchiever, maxCookOrPotionTask, 0.9f);
				genericGoalAchiever.setExecutableGoalAchiever(reservedItems -> {
					artifactGoalAchiever.clear();
					return artifactGoalAchiever.execute(reservedItems);
				});
			} else if (simCodeFound.length > 1) {
				updateGenericGoal(simCodeFound, characterService, goalAverageOptimizer);
				subGoal = new GoalAchieverTwoStep(characterDAO, genericGoalAchiever,
						new ForceExecuteGoalAchiever(subGoal), true, true);
			}

			GoalAchiever goalAchiever = new GoalAchieverTwoStep(characterDAO, depositNoReservedItemGoalAchiever,
					subGoal, true, true);

			return factoryCreator.createGoalAchieverLoop(goalAchiever, total);
		}
		return null;
	}

	private List<ArtifactGoalAchiever> initSimulation(String code, int total, BotCharacter botCharacter) {
		DepositNoReservedItemGoalAchiever simDepositNoReservedItemGoalAchiever = simulatorManager
				.getGoalFactoryCreator().createDepositNoReservedItemGoalAchiever();
		genericGoalAchiever.setCheckRealisableGoalAchiever(character -> false);
		genericGoalAchiever.setExecutableGoalAchiever(reservedItems -> false);
		GoalAchieverTwoStep goalAchieverTwoStep = new GoalAchieverTwoStep(simulatorManager.getCharacterDAOSimulator(),
				genericGoalAchiever, simulatedmonstersGoals.get(code), true, true);
		GoalAchiever goalAchiever = new GoalAchieverTwoStep(simulatorManager.getCharacterDAOSimulator(),
				simDepositNoReservedItemGoalAchiever, goalAchieverTwoStep, true, true);

		simGoalAchiever = factoryCreator.createGoalAchieverLoop(goalAchiever, total);

		Bornes bornes = new Bornes(1, 1, Math.min(botCharacter.getLevel(), botCharacter.getCookingLevel()));
		Predicate<ArtifactGoalAchiever> simulatedPredicateCook = StrategySkillUtils
				.createFilterCraftPredicate(simulatedGoalFactory, BotCraftSkill.COOKING, bornes);

		bornes = new Bornes(1, 1, Math.min(botCharacter.getLevel(), botCharacter.getAlchemyLevel()));
		Predicate<ArtifactGoalAchiever> simulatedPredicatePotion = StrategySkillUtils
				.createFilterCraftPredicate(simulatedGoalFactory, BotCraftSkill.ALCHEMY, bornes);

		List<ArtifactGoalAchiever> resultGoals = new ArrayList<>();
		resultGoals.addAll(cookAndAlchemySimulateGoals.stream().filter(simulatedPredicateCook).toList());
		resultGoals.addAll(cookAndAlchemySimulateGoals.stream().filter(simulatedPredicatePotion).toList());
		return resultGoals;
	}

	private String[] simulate(List<ArtifactGoalAchiever> testGoals, BotCharacter botCharacter,
			List<? extends BotItemReader> botItems) {
		LogManager.getLogManager().getLogger("").setLevel(Level.SEVERE);
		SumAccumulator accumulator = new SumAccumulator();
		simulatorListener
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));

		String[] foundGoalCode = new String[0];
		Map<String, Integer> reservedItems = new HashMap<>();
		simulatorManager.setValue(botCharacter, botItems);
		if (simGoalAchiever.isRealisableAfterSetRoot(simulatorManager.getCharacterDAOSimulator().getCharacter())) {
			simGoalAchiever.clear();
			boolean result = simGoalAchiever.execute(reservedItems);

			if (result) {
				int minTime = accumulator.get();

				for (ArtifactGoalAchiever artifactGoalAchiever : testGoals) {
					simulatorManager.setValue(botCharacter, botItems);
					accumulator.setMax(Integer.MAX_VALUE);// Pour ne pas planter dans l'optimisation
					genericGoalAchiever.setCheckRealisableGoalAchiever(
							character -> !simulatorManager.getCharacterServiceSimulator().isPossessOnSelf(
									simulatedGoalFactory.getInfos(artifactGoalAchiever).getItemCode()));
					simulateGoalAverageOptimizer.optimize(artifactGoalAchiever, maxCookOrPotionTask, 0.9f);
					genericGoalAchiever.setExecutableGoalAchiever(ri -> {
						artifactGoalAchiever.clear();
						boolean resultExec = artifactGoalAchiever.execute(ri);
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
								foundGoalCode = new String[] {
										simulatedGoalFactory.getInfos(artifactGoalAchiever).getItemCode() };
							}
						}
					} catch (StopSimulationException sse) {
						// On ne fait rien c'est normal
					}
				}
			}
		} else {
			int minTime = MAX_SIMULATION_TIME_IN_SECOND;
			Combinator<ArtifactGoalAchiever> combinator = new Combinator<>(ArtifactGoalAchiever.class, 3);
			List<ArtifactGoalAchiever> cookingGoal = testGoals.stream()
					.filter(aga -> BotCraftSkill.COOKING.equals(simulatedGoalFactory.getInfos(aga).getBotCraftSkill()))
					.toList();
			List<ArtifactGoalAchiever> potionGoal = testGoals.stream()
					.filter(aga -> BotCraftSkill.ALCHEMY.equals(simulatedGoalFactory.getInfos(aga).getBotCraftSkill()))
					.toList();
			List<ArtifactGoalAchiever> potionGoal2 = new ArrayList<>(potionGoal);
			combinator.set(0, cookingGoal);
			combinator.set(1, potionGoal);
			combinator.set(2, potionGoal2);

			for (ArtifactGoalAchiever[] artifactGA : combinator) {
				if (artifactGA[1] == artifactGA[2]) {
					continue;
				}

				simulatorManager.setValue(botCharacter, botItems);
				accumulator.setMax(Integer.MAX_VALUE); // Pour ne pas planter dans l'optimisation
				GoalAchiever testGoal = createGoals(artifactGA, simulatorManager.getCharacterServiceSimulator(),
						simulateGoalAverageOptimizer, simulatedGoalFactory);
				accumulator.reset();
				accumulator.setMax(minTime);
				try {
					if (testGoal.isRealisableAfterSetRoot(simulatorManager.getCharacterDAOSimulator().getCharacter())) {
						testGoal.clear();
						reservedItems.clear();
						if (testGoal.execute(reservedItems) && accumulator.get() < minTime) {
							minTime = accumulator.get();
							foundGoalCode = new String[] { simulatedGoalFactory.getInfos(artifactGA[0]).getItemCode(),
									simulatedGoalFactory.getInfos(artifactGA[1]).getItemCode(),
									simulatedGoalFactory.getInfos(artifactGA[2]).getItemCode() };
						}

					}
				} catch (StopSimulationException sse) {
					// On ne fait rien c'est normal
				}
			}
		}
		LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
		return foundGoalCode;
	}

	private GoalAchiever createGoals(ArtifactGoalAchiever[] artifactGA, CharacterService aCharacterService,
			GoalAverageOptimizer aGoalAverageOptimizer, GoalFactory factory) {
		genericGoalAchiever.setCheckRealisableGoalAchiever(character -> Arrays.stream(artifactGA)
				.<Boolean>map(aga -> !aCharacterService.isPossessOnSelf(factory.getInfos(aga).getItemCode()))
				.reduce(false, (v1, v2) -> v1 || v2));
		Arrays.stream(artifactGA).forEach(aga -> aGoalAverageOptimizer.optimize(aga, maxCookOrPotionTask, 0.9f));
		genericGoalAchiever.setExecutableGoalAchiever(ri -> {
			boolean resultExec = Arrays.stream(artifactGA).filter(aga -> {
				aga.clear();
				return true;
			}).map(aga -> !aCharacterService.isPossessOnSelf(factory.getInfos(aga).getItemCode()) && aga.execute(ri))
					.reduce(false, (v1, v2) -> v1 || v2);
			ri.clear();
			return resultExec;
		});
		return new ForceExecuteGoalAchiever(simGoalAchiever);
	}

	private void updateGenericGoal(String[] simCodeFound, CharacterService aCharacterService,
			GoalAverageOptimizer aGoalAverageOptimizer) {
		genericGoalAchiever.setCheckRealisableGoalAchiever(character -> Arrays.stream(simCodeFound)
				.<Boolean>map(code -> !aCharacterService.isPossessOnSelf(code)).reduce(false, (v1, v2) -> v1 || v2));
		Arrays.stream(simCodeFound).forEach(
				code -> aGoalAverageOptimizer.optimize(cookAndAlchemyGoals.get(code), maxCookOrPotionTask, 0.9f));
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
