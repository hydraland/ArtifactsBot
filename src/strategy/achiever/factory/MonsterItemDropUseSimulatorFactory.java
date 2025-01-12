package strategy.achiever.factory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.ForceExecuteGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverForLoop;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.GoalAverageOptimizerImpl;
import strategy.util.CharacterService;
import util.BinomialProbability;

public class MonsterItemDropUseSimulatorFactory extends UseSimulatorFactory implements MonsterItemDropFactory {
	private static final int MAX_SIMULATION_TIME_IN_SECOND = 172800;
	private final BankDAO bankDAO;
	private final CharacterService characterService;
	private final CharacterDAO characterDAO;
	private final Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals;
	private final GoalAverageOptimizer goalAverageOptimizer;
	private final float maxCookOrPotionTaskPercent;
	private final GoalFactoryCreator factoryCreator;
	private final Map<String, ArtifactGoalAchiever> simulatedDropItemGoals;

	public MonsterItemDropUseSimulatorFactory(Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals, BankDAO bankDAO,
			CharacterDAO characterDAO, ItemDAO itemDAO, GoalFactoryCreator factoryCreator,
			CharacterService characterService, SimulatorManager simulatorManager, GoalFactory simulatedGoalFactory,
			float maxCookOrPotionTaskPercent) {
		super(simulatorManager, factoryCreator, simulatedGoalFactory, maxCookOrPotionTaskPercent, cookAndAlchemyGoals,
				itemDAO);
		this.factoryCreator = factoryCreator;
		this.goalAverageOptimizer = new GoalAverageOptimizerImpl(characterDAO);
		this.maxCookOrPotionTaskPercent = maxCookOrPotionTaskPercent;
		this.cookAndAlchemyGoals = cookAndAlchemyGoals;
		this.bankDAO = bankDAO;
		this.characterDAO = characterDAO;
		this.characterService = characterService;
		this.simulatedDropItemGoals = simulatedGoalFactory.createDropItemGoal().stream()
				.collect(Collectors.toMap(this::createKey, t -> t.getGoal().getDropGoal()));
	}

	@Override
	public GoalAchiever createItemGoalAchiever(GoalAchieverInfo<ArtifactGoalAchiever> dropGoalInfo) {
		BotCharacter botCharacter = characterDAO.getCharacter();
		List<GoalAchieverInfo<ArtifactGoalAchiever>> testGoals = initSimulation(dropGoalInfo, botCharacter);
		String[] simCodeFound = simulate(testGoals, botCharacter, bankDAO.viewItems(), dropGoalInfo.getMonsterCode());
		GoalAchiever subGoal = dropGoalInfo.getGoal();
		if (simCodeFound.length == 1) {
			subGoal = factoryCreator.createGoalAchieverTwoStep(genericGoalAchiever, subGoal, false, true);
			genericGoalAchiever.setCheckRealisableGoalAchiever(c -> !characterService.isPossessOnSelf(simCodeFound[0]));
			ArtifactGoalAchiever artifactGoalAchiever = cookAndAlchemyGoals.get(simCodeFound[0]);
			int maxCookOrPotionTask = Math.round(maxCookOrPotionTaskPercent * botCharacter.getInventoryMaxItems());
			goalAverageOptimizer.optimize(artifactGoalAchiever, maxCookOrPotionTask, 0.9f);
			genericGoalAchiever.setExecutableGoalAchiever(reservedItems -> {
				artifactGoalAchiever.clear();
				boolean result = artifactGoalAchiever.execute(reservedItems);
				reservedItems.clear();
				return result;
			});
			genericGoalAchiever.setValue(artifactGoalAchiever);
		} else if (simCodeFound.length > 1) {
			int maxCookOrPotionTask = Math.round(maxCookOrPotionTaskPercent * botCharacter.getInventoryMaxItems() / 3);
			updateGenericGoal(simCodeFound, characterService, goalAverageOptimizer, maxCookOrPotionTask);
			subGoal = factoryCreator.createGoalAchieverTwoStep(genericGoalAchiever,
					new ForceExecuteGoalAchiever(subGoal), false, true);
		}

		return factoryCreator.createGoalAchieverLoop(subGoal, 1, false);
	}

	private List<GoalAchieverInfo<ArtifactGoalAchiever>> initSimulation(
			GoalAchieverInfo<ArtifactGoalAchiever> dropGoalInfo, BotCharacter botCharacter) {
		GoalFactoryCreator simulatorFactoryCreator = simulatorManager.getGoalFactoryCreator();
		GoalAchiever goalAchieverTwoStep = simulatorFactoryCreator.createGoalAchieverTwoStep(genericGoalAchiever,
				simulatedDropItemGoals.get(createKey(dropGoalInfo)), false, true);
		simGoalAchiever = new GoalAchieverForLoop(goalAchieverTwoStep,
				BinomialProbability.calculateNbTentative(0.9, 1, dropGoalInfo.getGoal().getRate(),
						100));
		return initSimulation(botCharacter);
	}

	@Override
	protected int getMaxSimulationTime() {
		return MAX_SIMULATION_TIME_IN_SECOND;
	}

	private String createKey(GoalAchieverInfo<?> info) {
		return info.getMonsterCode() + info.getItemCode();
	}
}
