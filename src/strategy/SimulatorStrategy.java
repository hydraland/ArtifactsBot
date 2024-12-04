package strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.dao.simulate.StopSimulationException;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotItem;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.ArtifactGoalAchiever;
import strategy.achiever.factory.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.GoalAchieverInfo;
import strategy.achiever.factory.GoalAchieverLoop;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.MonsterGoalAchiever;
import strategy.achiever.factory.util.GameService;
import strategy.util.Bornes;
import strategy.util.CharacterService;
import strategy.util.StrategySkillUtils;

public class SimulatorStrategy implements Strategy {

	private static final int NUMBER_OF_SIMULATE = 2;
	private static final int MAX_MULTIPLIER_COEFFICIENT = 5;
	private final SimulatorManager simulatorManager;
	private final CharacterDAO characterDAO;
	private final GoalFactory goalFactory;
	private final BankDAO bankDAO;
	private final CharacterService characterService;
	private final List<ArtifactGoalAchiever> itemGoals;
	private final List<GoalAchiever> inventoryGoals;
	private final List<GoalAchiever> taskGoals;
	private final List<ArtifactGoalAchiever> dropItemGoal;
	private GoalAchiever eventGoal;
	private final List<ArtifactGoalAchiever> itemSimulatedGoals;
	private final GoalFactory simulatedGoalFactory;
	private final StrategySimulatorListener simulatorListener;
	private final GameService gameService;
	private final List<MonsterGoalAchiever> monsterGoalsForEvent;

	public SimulatorStrategy(SimulatorManager simulatorManager, StrategySimulatorListener simulatorListener,
			CharacterDAO characterDAO, BankDAO bankDAO, GoalFactory goalFactory, CharacterService characterService,
			GameService gameService, GoalParameter parameter) {
		this.simulatorManager = simulatorManager;
		this.simulatorListener = simulatorListener;
		this.characterDAO = characterDAO;
		this.bankDAO = bankDAO;
		this.goalFactory = goalFactory;
		this.characterService = characterService;
		this.gameService = gameService;
		itemGoals = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING);
		simulatedGoalFactory = simulatorManager.createFactory(parameter);
		itemSimulatedGoals = simulatedGoalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING);
		inventoryGoals = goalFactory.createManagedInventoryCustomGoal();
		monsterGoalsForEvent = goalFactory.createMonstersGoals(resp -> false);
		taskGoals = goalFactory.createTaskGoals(resp -> !resp.fight().isWin());
		dropItemGoal = goalFactory.getDropItemGoal();
	}

	@Override
	public Iterable<GoalAchiever> getGoalAchievers() {
		LogManager.getLogManager().getLogger("").setLevel(Level.SEVERE);
		BotCharacter character = this.characterDAO.getCharacter();
		List<ArtifactGoalAchiever> allGoals = Strategy.filterTaskGoals(itemGoals, characterService, goalFactory,
				bankDAO);
		List<ArtifactGoalAchiever> allSimulateGoals = Strategy.filterTaskGoals(itemSimulatedGoals,
				simulatorManager.getCharacterServiceSimulator(), simulatedGoalFactory,
				simulatorManager.getBankDAOSimulator());

		List<GoalAchiever> goalAchievers = new ArrayList<>();
		ArtifactGoalAchiever searchMiningGoalAchiever = searchMiningGoal(character, allGoals);
		ArtifactGoalAchiever searchWoodcuttingGoalAchiever = searchWoodcuttingGoal(character, allGoals);
		addMiningAndWoodcuttingGoals(goalAchievers, searchMiningGoalAchiever, searchWoodcuttingGoalAchiever);
		ArtifactGoalAchiever searchFishingGoalAchiever = searchFishingGoal(character, allGoals);
		if (searchFishingGoalAchiever != null) {
			goalAchievers.add(searchFishingGoalAchiever);
		}
		List<ArtifactGoalAchiever> searchCookingGoalAchiever = searchCookingGoal(character, allGoals);
		goalAchievers.addAll(searchCookingGoalAchiever);
		addMiningAndWoodcuttingGoals(goalAchievers, searchMiningGoalAchiever, searchWoodcuttingGoalAchiever);
		List<ArtifactGoalAchiever> searchAlchemyGoalAchiever = searchAlchemyGoal(character, allGoals);
		goalAchievers.addAll(searchAlchemyGoalAchiever);
		addMiningAndWoodcuttingGoals(goalAchievers, searchMiningGoalAchiever, searchWoodcuttingGoalAchiever);
		int maxLevel = Math.max(character.getWeaponcraftingLevel(),
				Math.max(character.getGearcraftingLevel(), character.getJewelrycraftingLevel()));
		List<ArtifactGoalAchiever> searchGearGoalAchiever = searchGearGoal(character, allGoals, allSimulateGoals,
				maxLevel);
		goalAchievers.addAll(searchGearGoalAchiever);
		addMiningAndWoodcuttingGoals(goalAchievers, searchMiningGoalAchiever, searchWoodcuttingGoalAchiever);
		List<ArtifactGoalAchiever> searchWeaponGoalAchiever = searchWeaponGoal(character, allGoals, allSimulateGoals,
				maxLevel);
		goalAchievers.addAll(searchWeaponGoalAchiever);
		addMiningAndWoodcuttingGoals(goalAchievers, searchMiningGoalAchiever, searchWoodcuttingGoalAchiever);
		List<ArtifactGoalAchiever> searchJewelryGoalAchiever = searchJewelryGoal(character, allGoals, allSimulateGoals,
				maxLevel);
		goalAchievers.addAll(searchJewelryGoalAchiever);
		addMiningAndWoodcuttingGoals(goalAchievers, searchMiningGoalAchiever, searchWoodcuttingGoalAchiever);
		goalAchievers.addAll(taskGoals);
		goalAchievers.addAll(taskGoals);
		addMiningAndWoodcuttingGoals(goalAchievers, searchMiningGoalAchiever, searchWoodcuttingGoalAchiever);
		goalAchievers.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, goalFactory, bankDAO));
		LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
		return goalAchievers;
	}

	private void addMiningAndWoodcuttingGoals(List<GoalAchiever> goalAchievers,
			ArtifactGoalAchiever searchMiningGoalAchiever, ArtifactGoalAchiever searchWoodcuttingGoalAchiever) {
		if (searchMiningGoalAchiever != null) {
			goalAchievers.add(searchMiningGoalAchiever);
		}
		if (searchWoodcuttingGoalAchiever != null) {
			goalAchievers.add(searchWoodcuttingGoalAchiever);
		}
	}

	private List<ArtifactGoalAchiever> searchAlchemyGoal(BotCharacter character, List<ArtifactGoalAchiever> allGoals) {
		int minSkillLevel = Math.max(1, character.getAlchemyLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
		Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getAlchemyLevel() + 1);
		Predicate<ArtifactGoalAchiever> predicate = StrategySkillUtils.createFilterResourceAndCraftPredicate(
				goalFactory, bornes, BotResourceSkill.ALCHEMY, BotCraftSkill.ALCHEMY);
		return allGoals.stream().filter(predicate).toList();
	}

	private List<ArtifactGoalAchiever> searchCookingGoal(BotCharacter character, List<ArtifactGoalAchiever> allGoals) {
		int minSkillLevel = Math.max(1, character.getCookingLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
		Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getCookingLevel() + 1);
		Predicate<ArtifactGoalAchiever> predicate = StrategySkillUtils.createFilterCraftPredicate(goalFactory,
				BotCraftSkill.COOKING, bornes);
		List<ArtifactGoalAchiever> cookingResult = allGoals.stream().filter(predicate).toList();
		cookingResult.forEach(aga -> optimize(aga, goalFactory));
		return cookingResult;
	}

	private List<ArtifactGoalAchiever> searchJewelryGoal(BotCharacter character, List<ArtifactGoalAchiever> allGoals,
			List<ArtifactGoalAchiever> allSimulateGoals, int maxLevel) {
		int craftingLevel = character.getJewelrycraftingLevel();
		BotCraftSkill craftSkill = BotCraftSkill.JEWELRYCRAFTING;
		return searchGoal(character, allGoals, allSimulateGoals, craftingLevel, craftSkill, goal -> {
			GoalAchieverInfo infos = goalFactory.getInfos(goal);
			if (BotItemType.RING.equals(infos.getItemType())) {
				return !characterService.isPossess(infos.getItemCode(), 2, bankDAO);
			} else {
				return !characterService.isPossess(infos.getItemCode(), bankDAO);
			}
		}, maxLevel);
	}

	private List<ArtifactGoalAchiever> searchGearGoal(BotCharacter character, List<ArtifactGoalAchiever> allGoals,
			List<ArtifactGoalAchiever> allSimulateGoals, int maxLevel) {
		int craftingLevel = character.getGearcraftingLevel();
		BotCraftSkill craftSkill = BotCraftSkill.GEARCRAFTING;
		return searchGoal(character, allGoals, allSimulateGoals, craftingLevel, craftSkill, goal -> {
			GoalAchieverInfo infos = goalFactory.getInfos(goal);
			return !characterService.isPossess(infos.getItemCode(), bankDAO);
		}, maxLevel);
	}

	private List<ArtifactGoalAchiever> searchWeaponGoal(BotCharacter character, List<ArtifactGoalAchiever> allGoals,
			List<ArtifactGoalAchiever> allSimulateGoals, int maxLevel) {
		int craftingLevel = character.getWeaponcraftingLevel();
		BotCraftSkill craftSkill = BotCraftSkill.WEAPONCRAFTING;
		return searchGoal(character, allGoals, allSimulateGoals, craftingLevel, craftSkill, goal -> {
			GoalAchieverInfo infos = goalFactory.getInfos(goal);
			return !characterService.isPossess(infos.getItemCode(), bankDAO);
		}, maxLevel);
	}

	private List<ArtifactGoalAchiever> searchGoal(BotCharacter character, List<ArtifactGoalAchiever> allGoals,
			List<ArtifactGoalAchiever> allSimulateGoals, int craftingLevel, BotCraftSkill craftSkill,
			Predicate<? super ArtifactGoalAchiever> predicateFilterPossesed, int maxLevel) {
		if (craftingLevel == GameConstants.MAX_LEVEL) {
			int minSkillLevel = craftingLevel - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP;
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, craftingLevel + 1);
			Predicate<ArtifactGoalAchiever> predicate = StrategySkillUtils.createFilterCraftPredicate(goalFactory,
					craftSkill, bornes);
			return allGoals.stream().filter(predicate).filter(predicateFilterPossesed).toList();
		}
		if (craftingLevel % GameConstants.STEP_LEVEL == 0) {
			Bornes bornes = new Bornes(craftingLevel, craftingLevel, craftingLevel + 1);
			Predicate<ArtifactGoalAchiever> predicate = StrategySkillUtils.createFilterCraftPredicate(goalFactory,
					craftSkill, bornes);
			List<ArtifactGoalAchiever> resultList = allGoals.stream().filter(predicate).filter(predicateFilterPossesed)
					.filter(aga -> aga.isRealisableAfterSetRoot(character)).toList();
			if (!resultList.isEmpty()) {
				return resultList;
			}
		}
		int minSkillLevel = Math.max(1, craftingLevel - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
		Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, craftingLevel + 1);
		// On filtre le plus rapide??
		int minTime = Integer.MAX_VALUE;
		String goalCode = "";
		Predicate<ArtifactGoalAchiever> simulatedPredicate = StrategySkillUtils
				.createFilterCraftPredicate(simulatedGoalFactory, craftSkill, bornes);
		List<ArtifactGoalAchiever> simGoals = allSimulateGoals.stream().filter(simulatedPredicate).toList();
		List<BotItem> bankItems = bankDAO.viewItems();
		SumAccumulator accumulator = new SumAccumulator();
		simulatorListener
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));
		for (ArtifactGoalAchiever simGoal : simGoals) {
			boolean success = true;
			optimize(simGoal, simulatedGoalFactory);
			accumulator.reset();
			try {
				for (int i = 0; i < NUMBER_OF_SIMULATE; i++) {
					simulatorManager.setValue(character, bankItems);
					if (simGoal.isRealisableAfterSetRoot(character)) {
						simGoal.clear();
						if (!simGoal.execute(new HashMap<>())) {
							success = false;
							break;
						}
					} else {
						success = false;
						break;
					}
				}
				if (success && minTime > accumulator.get()) {
					minTime = accumulator.get();
					goalCode = simulatedGoalFactory.getInfos(simGoal).getItemCode();
				}
			} catch (StopSimulationException sse) {
				// Arrêt de la simulation
			}
		}
		String goalCodeFound = goalCode;
		Optional<ArtifactGoalAchiever> searchRealGoal = allGoals.stream()
				.filter(aga -> goalFactory.getInfos(aga).getItemCode().equals(goalCodeFound)).findFirst();
		List<ArtifactGoalAchiever> result = new ArrayList<>();
		if (searchRealGoal.isPresent()) {
			optimize(searchRealGoal.get(), goalFactory);
			result.add(searchRealGoal.get());
			if (craftingLevel < maxLevel) {
				result.add(searchRealGoal.get());
			}
		}
		return result;
	}

	private ArtifactGoalAchiever searchFishingGoal(BotCharacter character, List<ArtifactGoalAchiever> allGoals) {
		if (character.getFishingLevel() < GameConstants.MAX_LEVEL) {
			int minSkillLevel = Math.max(1, character.getFishingLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getFishingLevel() + 1);
			Predicate<ArtifactGoalAchiever> predicate = StrategySkillUtils.createFilterResourcePredicate(goalFactory,
					bornes, BotResourceSkill.FISHING);

			Optional<ArtifactGoalAchiever> resultGoal = allGoals.stream().filter(predicate).max((aga1, aga2) -> Integer
					.compare(goalFactory.getInfos(aga1).getLevel(), goalFactory.getInfos(aga2).getLevel()));
			if (resultGoal.isPresent()) {
				GoalAchieverLoop returnGoal = new GoalAchieverLoop(resultGoal.get(), 10);
				if (characterService.isPossessAny(gameService.getToolsCode(BotResourceSkill.FISHING), bankDAO)) {
					optimize(returnGoal, goalFactory);
				}
				return returnGoal;
			}
		}
		return null;
	}

	private ArtifactGoalAchiever searchWoodcuttingGoal(BotCharacter character, List<ArtifactGoalAchiever> allGoals) {
		if (character.getWoodcuttingLevel() < GameConstants.EVENT_RESOURCE_LEVEL) {
			int minSkillLevel = Math.max(1,
					character.getWoodcuttingLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getWoodcuttingLevel() + 1);
			Predicate<ArtifactGoalAchiever> predicate = StrategySkillUtils.createFilterCraftPredicate(goalFactory,
					BotCraftSkill.WOODCUTTING, bornes);
			Optional<ArtifactGoalAchiever> resultGoal = allGoals.stream().filter(predicate).max((aga1, aga2) -> Integer
					.compare(goalFactory.getInfos(aga1).getLevel(), goalFactory.getInfos(aga2).getLevel()));
			if (resultGoal.isPresent()) {
				if (characterService.isPossessAny(gameService.getToolsCode(BotResourceSkill.WOODCUTTING), bankDAO)) {
					GoalAchieverLoop returnGoal = new GoalAchieverLoop(resultGoal.get(), 5);
					optimize(returnGoal, goalFactory);
					return returnGoal;
				}
				return resultGoal.get();
			}
		}
		return null;
	}

	private ArtifactGoalAchiever searchMiningGoal(BotCharacter character, List<ArtifactGoalAchiever> allGoals) {
		if (character.getMiningLevel() < GameConstants.EVENT_RESOURCE_LEVEL) {
			int minSkillLevel = Math.max(1, character.getMiningLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getMiningLevel() + 1);
			Predicate<ArtifactGoalAchiever> predicate = StrategySkillUtils.createFilterCraftPredicate(goalFactory,
					BotCraftSkill.MINING, bornes);
			Optional<ArtifactGoalAchiever> resultGoal = allGoals.stream().filter(predicate).max((aga1, aga2) -> Integer
					.compare(goalFactory.getInfos(aga1).getLevel(), goalFactory.getInfos(aga2).getLevel()));
			if (resultGoal.isPresent()) {
				if (characterService.isPossessAny(gameService.getToolsCode(BotResourceSkill.MINING), bankDAO)) {
					GoalAchieverLoop returnGoal = new GoalAchieverLoop(resultGoal.get(), 5);
					optimize(returnGoal, goalFactory);
					return returnGoal;
				}
				return resultGoal.get();
			}
		}
		return null;
	}

	@Override
	public Iterable<GoalAchiever> getManagedInventoryCustomGoal() {
		return inventoryGoals;
	}

	@Override
	public boolean isAcceptEvent(String type, String code) {
		return Strategy.isAcceptEvent(goalFactory, characterDAO, type, code, monsterGoalsForEvent, itemGoals);
	}

	@Override
	public void initializeGoal(String type, String code) {
		eventGoal = Strategy.initializeGoal(goalFactory, type, code, monsterGoalsForEvent, itemGoals);

	}

	@Override
	public GoalAchiever getEventGoalAchiever() {
		return eventGoal;
	}

	private void optimize(ArtifactGoalAchiever goalAchiever, GoalFactory goalFactory) {
		if (goalFactory.getInfos(goalAchiever) == null || !goalFactory.getInfos(goalAchiever).isNeedTaskMasterResource()
				&& !goalFactory.getInfos(goalAchiever).isNeedRareResource()) {
			goalFactory.getGoalAverageOptimizer().optimize(goalAchiever, MAX_MULTIPLIER_COEFFICIENT, 0.9f);
		} else {
			goalFactory.getGoalAverageOptimizer().optimize(goalAchiever, 1, 1f);
		}
	}
}
