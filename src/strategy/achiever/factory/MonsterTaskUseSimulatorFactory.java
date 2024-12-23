package strategy.achiever.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.model.BotCharacter;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.GoalFactory.GoalFilter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.ForceExecuteGoalAchiever;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.GoalAverageOptimizerImpl;
import strategy.util.CharacterService;

public class MonsterTaskUseSimulatorFactory extends UseSimulatorFactory implements MonsterTaskFactory {

	private static final int MAX_SIMULATION_TIME_IN_SECOND = 86400;
	private final Map<String, MonsterGoalAchiever> monsterGoals;
	private final Map<String, MonsterGoalAchiever> simulatedmonstersGoals;
	private final BankDAO bankDAO;
	private final CharacterService characterService;
	private final CharacterDAO characterDAO;
	private final Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals;
	private final GoalAverageOptimizer goalAverageOptimizer;
	private final float maxCookOrPotionTaskPercent;
	private final GoalAchiever depositNoReservedItemGoalAchiever;
	private final GoalFactoryCreator factoryCreator;

	public MonsterTaskUseSimulatorFactory(Map<String, MonsterGoalAchiever> monsterGoals,
			Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals, BankDAO bankDAO, CharacterDAO characterDAO,
			GoalFactoryCreator factoryCreator, CharacterService characterService, SimulatorManager simulatorManager,
			GoalFactory simulatedGoalFactory, float maxCookOrPotionTaskPercent) {
		super(simulatorManager, factoryCreator, simulatedGoalFactory, maxCookOrPotionTaskPercent, cookAndAlchemyGoals);
		this.factoryCreator = factoryCreator;
		this.goalAverageOptimizer = new GoalAverageOptimizerImpl(characterDAO);
		this.maxCookOrPotionTaskPercent = maxCookOrPotionTaskPercent;
		this.cookAndAlchemyGoals = cookAndAlchemyGoals;
		this.monsterGoals = monsterGoals;
		this.bankDAO = bankDAO;
		this.characterDAO = characterDAO;
		this.characterService = characterService;
		simulatedmonstersGoals = simulatedGoalFactory.createMonstersGoals(resp -> !resp.fight().isWin(), GoalFilter.ALL)
				.stream().collect(Collectors.toMap(MonsterGoalAchiever::getMonsterCode, Function.identity()));
		depositNoReservedItemGoalAchiever = factoryCreator.createDepositNoReservedItemGoalAchiever();
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total) {
		if (monsterGoals.containsKey(code)) {
			BotCharacter botCharacter = characterDAO.getCharacter();
			List<GoalAchieverInfo<ArtifactGoalAchiever>> testGoals = initSimulation(code, total, botCharacter);

			String[] simCodeFound = simulate(testGoals, botCharacter, bankDAO.viewItems());
			GoalAchiever subGoal = monsterGoals.get(code);
			if (simCodeFound.length == 1) {
				subGoal = factoryCreator.createGoalAchieverTwoStep(genericGoalAchiever, subGoal, false, true);
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
				genericGoalAchiever.setValue(artifactGoalAchiever);
			} else if (simCodeFound.length > 1) {
				int maxCookOrPotionTask = Math
						.round(maxCookOrPotionTaskPercent * botCharacter.getInventoryMaxItems() / 3);
				updateGenericGoal(simCodeFound, characterService, goalAverageOptimizer, maxCookOrPotionTask);
				subGoal = factoryCreator.createGoalAchieverTwoStep(genericGoalAchiever,
						new ForceExecuteGoalAchiever(subGoal), false, true);
			}

			GoalAchiever goalAchiever = factoryCreator.createGoalAchieverTwoStep(depositNoReservedItemGoalAchiever,
					subGoal, false, true);

			return factoryCreator.createGoalAchieverLoop(goalAchiever, total, false);
		}
		return null;
	}

	private List<GoalAchieverInfo<ArtifactGoalAchiever>> initSimulation(String code, int total, BotCharacter botCharacter) {
		GoalFactoryCreator simulatorFactoryCreator = simulatorManager.getGoalFactoryCreator();
		GoalAchiever simDepositNoReservedItemGoalAchiever = simulatorFactoryCreator
				.createDepositNoReservedItemGoalAchiever();
		GoalAchiever goalAchieverTwoStep = simulatorFactoryCreator.createGoalAchieverTwoStep(genericGoalAchiever,
				simulatedmonstersGoals.get(code), false, true);
		GoalAchiever goalAchiever = simulatorFactoryCreator.createGoalAchieverTwoStep(simDepositNoReservedItemGoalAchiever,
				goalAchieverTwoStep, false, true);
		simGoalAchiever = factoryCreator.createGoalAchieverLoop(goalAchiever, total, false);
		return initSimulation(botCharacter);
	}

	@Override
	protected int getMaxSimulationTime() {
		return MAX_SIMULATION_TIME_IN_SECOND;
	}
}
