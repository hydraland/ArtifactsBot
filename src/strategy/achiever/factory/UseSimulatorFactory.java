package strategy.achiever.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.GoalAverageOptimizerImpl;
import strategy.util.Bornes;
import strategy.util.CharacterService;
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

	protected UseSimulatorFactory(SimulatorManager simulatorManager, GoalFactoryCreator factoryCreator,
			GoalFactory simulatedGoalFactory, float maxCookOrPotionTaskPercent,
			Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals) {
		this.simulatorManager = simulatorManager;
		this.maxCookOrPotionTaskPercent = maxCookOrPotionTaskPercent;
		this.cookAndAlchemyGoals = cookAndAlchemyGoals;
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
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Combinator<GoalAchieverInfo<ArtifactGoalAchiever>> combinator = new Combinator(GoalAchieverInfo.class, 3);
		List<GoalAchieverInfo<ArtifactGoalAchiever>> cookingGoal = testGoals.stream()
				.filter(aga -> BotCraftSkill.COOKING.equals(aga.getBotCraftSkill())).toList();
		List<GoalAchieverInfo<ArtifactGoalAchiever>> potionGoal = testGoals.stream()
				.filter(aga -> BotCraftSkill.ALCHEMY.equals(aga.getBotCraftSkill())).toList();
		List<GoalAchieverInfo<ArtifactGoalAchiever>> potionGoal2 = new ArrayList<>(potionGoal);
		combinator.set(0, cookingGoal);
		combinator.set(1, potionGoal);
		combinator.set(2, potionGoal2);

		for (GoalAchieverInfo<ArtifactGoalAchiever>[] artifactGA : combinator) {
			if (artifactGA[1] == artifactGA[2]) {
				continue;
			}

			simulatorManager.setValue(botCharacter, botItems);
			accumulator.setMax(Integer.MAX_VALUE); // Pour ne pas planter dans l'optimisation
			int maxCookOrPotionTask = Math.round(maxCookOrPotionTaskPercent * botCharacter.getInventoryMaxItems() / 3);
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
		return foundGoalCode;
	}

	protected abstract int getMaxSimulationTime();

	protected final GoalAchiever createGoals(GoalAchieverInfo<ArtifactGoalAchiever>[] artifactGA,
			CharacterService aCharacterService, GoalAverageOptimizer aGoalAverageOptimizer, int optimizeValue) {
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
		genericGoalAchiever.setValue(artifactGA);
		return new ForceExecuteGoalAchiever(simGoalAchiever);
	}

	protected final void updateGenericGoal(String[] simCodeFound, CharacterService aCharacterService,
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
		genericGoalAchiever.setValue(simCodeFound);
	}
}
