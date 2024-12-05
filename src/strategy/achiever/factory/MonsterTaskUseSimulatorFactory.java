package strategy.achiever.factory;

import java.util.ArrayList;
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
import hydra.model.BotItem;
import strategy.StrategySimulatorListener;
import strategy.SumAccumulator;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.GenericGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.GoalAchieverTwoStep;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.util.Bornes;
import strategy.util.CharacterService;
import strategy.util.MoveService;
import strategy.util.StrategySkillUtils;

public class MonsterTaskUseSimulatorFactory implements MonsterTaskFactory {

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
	private final GoalParameter simulatedGoalParameter;
	private final DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever;

	public MonsterTaskUseSimulatorFactory(Map<String, MonsterGoalAchiever> monsterGoals,
			Map<String, ArtifactGoalAchiever> cookAndAlchemyGoals, BankDAO bankDAO, CharacterDAO characterDAO,
			MoveService moveService, CharacterService characterService, SimulatorManager simulatorManager,
			StrategySimulatorListener simulatorListener, GoalAverageOptimizer goalAverageOptimizer,
			GoalFactory simulatedGoalFactory, GoalParameter goalParameter, GoalParameter simulatedGoalParameter,
			int maxCookOrPotionTask) {
		this.goalAverageOptimizer = goalAverageOptimizer;
		this.simulatorListener = simulatorListener;
		this.simulatedGoalParameter = simulatedGoalParameter;
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
		depositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(bankDAO, moveService,
				characterService, goalParameter);
	}

	@Override
	public GoalAchiever createTaskGoalAchiever(String code, int total) {
		if (monsterGoals.containsKey(code)) {
			List<ArtifactGoalAchiever> testGoals = initSimulation(code, total, characterDAO.getCharacter());

			String simCodeFound = simulate(testGoals, characterDAO.getCharacter(), bankDAO.viewItems());
			GoalAchiever subGoal = monsterGoals.get(code);
			if (simCodeFound != null) {
				subGoal = new GoalAchieverTwoStep(characterDAO, genericGoalAchiever, subGoal, true, true);
				genericGoalAchiever.setCheckRealisableGoalAchiever(
						character -> !characterService.inventoryConstaints(simCodeFound, 1));
				ArtifactGoalAchiever artifactGoalAchiever = cookAndAlchemyGoals.get(simCodeFound);
				goalAverageOptimizer.optimize(artifactGoalAchiever, maxCookOrPotionTask, 0.9f);
				genericGoalAchiever.setExecutableGoalAchiever(artifactGoalAchiever::execute);
			}

			GoalAchiever goalAchiever = new GoalAchieverTwoStep(characterDAO, depositNoReservedItemGoalAchiever,
					subGoal, true, true);

			return new GoalAchieverLoop(goalAchiever, total);
		}
		return null;
	}

	private List<ArtifactGoalAchiever> initSimulation(String code, int total, BotCharacter botCharacter) {
		DepositNoReservedItemGoalAchiever simDepositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(
				simulatorManager.getBankDAOSimulator(), simulatorManager.getMoveService(),
				simulatorManager.getCharacterServiceSimulator(), simulatedGoalParameter);
		genericGoalAchiever.setCheckRealisableGoalAchiever(character -> false);
		genericGoalAchiever.setExecutableGoalAchiever(reservedItems -> false);
		GoalAchieverTwoStep goalAchieverTwoStep = new GoalAchieverTwoStep(simulatorManager.getCharacterDAOSimulator(),
				genericGoalAchiever, simulatedmonstersGoals.get(code), true, true);
		GoalAchiever goalAchiever = new GoalAchieverTwoStep(simulatorManager.getCharacterDAOSimulator(),
				simDepositNoReservedItemGoalAchiever, goalAchieverTwoStep, true, true);

		simGoalAchiever = new GoalAchieverLoop(goalAchiever, total);

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

	private String simulate(List<ArtifactGoalAchiever> testGoals, BotCharacter botCharacter, List<BotItem> botItems) {
		LogManager.getLogManager().getLogger("").setLevel(Level.SEVERE);
		SumAccumulator accumulator = new SumAccumulator();
		simulatorListener
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));

		String foundGoalCode = null;
		Map<String, Integer> reservedItems = new HashMap<>();
		simulatorManager.setValue(botCharacter, botItems);
		if (simGoalAchiever.isRealisableAfterSetRoot(simulatorManager.getCharacterDAOSimulator().getCharacter())) {
			simGoalAchiever.clear();
			boolean result = simGoalAchiever.execute(reservedItems);

			if (result) {
				int minTime = accumulator.get();

				for (ArtifactGoalAchiever artifactGoalAchiever : testGoals) {
					simulatorManager.setValue(botCharacter, botItems);
					genericGoalAchiever.setCheckRealisableGoalAchiever(
							character -> !simulatorManager.getCharacterServiceSimulator().inventoryConstaints(
									simulatedGoalFactory.getInfos(artifactGoalAchiever).getItemCode(), 1));
					goalAverageOptimizer.optimize(artifactGoalAchiever, maxCookOrPotionTask, 0.9f);
					genericGoalAchiever.setExecutableGoalAchiever(ri -> {boolean resultExec = artifactGoalAchiever.execute(ri); ri.clear(); return resultExec;});
					accumulator.reset();
					accumulator.setMax(minTime);
					if (simGoalAchiever.isRealisableAfterSetRoot(simulatorManager.getCharacterDAOSimulator().getCharacter())) {
						simGoalAchiever.clear();
						reservedItems.clear();
						try {
							if (simGoalAchiever.execute(reservedItems) && accumulator.get() < minTime) {
								minTime = accumulator.get();
								foundGoalCode = simulatedGoalFactory.getInfos(artifactGoalAchiever).getItemCode();
							}
						} catch (StopSimulationException sse) {
							// On ne fait rien c'est normal
						}
					}
				}
			}
		}
		LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
		return foundGoalCode;
	}
}
