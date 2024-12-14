package strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.dao.simulate.StopSimulationException;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotItemReader;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.GoalFactory.GoalFilter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.achiever.factory.util.ItemService;
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
	private final Collection<GoalAchieverInfo> itemGoals;
	private final Collection<GoalAchieverInfo> itemGoalsForEvent;
	private final List<GoalAchiever> inventoryGoals;
	private final List<GoalAchiever> taskGoals;
	private final List<GoalAchieverInfo> dropItemGoal;
	private GoalAchiever eventGoal;
	private final Collection<GoalAchieverInfo> itemSimulatedGoals;
	private final ItemService itemService;
	private final List<MonsterGoalAchiever> monsterGoalsForEvent;
	private final GoalAverageOptimizer goalAverageOptimizer;

	public SimulatorStrategy(SimulatorManager simulatorManager, CharacterDAO characterDAO, BankDAO bankDAO,
			GoalFactory goalFactory, CharacterService characterService, ItemService itemService,
			GoalFactory simulatedGoalFactory, GoalAverageOptimizer goalAverageOptimizer) {
		this.simulatorManager = simulatorManager;
		this.characterDAO = characterDAO;
		this.bankDAO = bankDAO;
		this.goalFactory = goalFactory;
		this.characterService = characterService;
		this.itemService = itemService;
		this.goalAverageOptimizer = goalAverageOptimizer;

		itemGoals = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING,
				GoalFilter.NO_EVENT);
		itemGoalsForEvent = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.GATHERING, GoalFilter.EVENT);
		itemSimulatedGoals = simulatedGoalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING,
				GoalFilter.NO_EVENT);
		inventoryGoals = goalFactory.createManagedInventoryCustomGoal();
		monsterGoalsForEvent = goalFactory.createMonstersGoals(resp -> false, GoalFilter.EVENT);
		taskGoals = goalFactory.createTaskGoals();
		dropItemGoal = goalFactory.getDropItemGoal();
	}

	@Override
	public Deque<GoalAchiever> getGoalAchievers() {
		BotCharacter character = this.characterDAO.getCharacter();
		List<GoalAchieverInfo> allGoals = Strategy.filterTaskGoals(itemGoals, characterService, bankDAO);
		List<GoalAchieverInfo> allSimulateGoals = Strategy.filterTaskGoals(itemSimulatedGoals,
				simulatorManager.getCharacterServiceSimulator(), simulatorManager.getBankDAOSimulator());

		Deque<GoalAchiever> goalAchievers = new LinkedList<>();
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
		goalAchievers.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, bankDAO));
		return goalAchievers;
	}

	private void addMiningAndWoodcuttingGoals(Deque<GoalAchiever> goalAchievers,
			ArtifactGoalAchiever searchMiningGoalAchiever, ArtifactGoalAchiever searchWoodcuttingGoalAchiever) {
		if (searchMiningGoalAchiever != null) {
			goalAchievers.add(searchMiningGoalAchiever);
		}
		if (searchWoodcuttingGoalAchiever != null) {
			goalAchievers.add(searchWoodcuttingGoalAchiever);
		}
	}

	private List<ArtifactGoalAchiever> searchAlchemyGoal(BotCharacter character, List<GoalAchieverInfo> allGoals) {
		int minSkillLevel = Math.max(1, character.getAlchemyLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
		Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getAlchemyLevel() + 1);
		Predicate<GoalAchieverInfo> predicate = StrategySkillUtils.createFilterResourceAndCraftPredicate(bornes,
				BotResourceSkill.ALCHEMY, BotCraftSkill.ALCHEMY);
		return allGoals.stream().filter(predicate).map(GoalAchieverInfo::getGoal).toList();
	}

	private List<ArtifactGoalAchiever> searchCookingGoal(BotCharacter character, List<GoalAchieverInfo> allGoals) {
		int minSkillLevel = Math.max(1, character.getCookingLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
		Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getCookingLevel() + 1);
		Predicate<GoalAchieverInfo> predicate = StrategySkillUtils.createFilterCraftPredicate(BotCraftSkill.COOKING,
				bornes);
		List<GoalAchieverInfo> cookingResult = allGoals.stream().filter(predicate).toList();
		cookingResult.forEach(this::optimize);
		return cookingResult.stream().map(GoalAchieverInfo::getGoal).toList();
	}

	private List<ArtifactGoalAchiever> searchJewelryGoal(BotCharacter character, List<GoalAchieverInfo> allGoals,
			List<GoalAchieverInfo> allSimulateGoals, int maxLevel) {
		int craftingLevel = character.getJewelrycraftingLevel();
		BotCraftSkill craftSkill = BotCraftSkill.JEWELRYCRAFTING;
		return searchGoal(character, allGoals, allSimulateGoals, craftingLevel, craftSkill, infos -> {
			if (BotItemType.RING.equals(infos.getItemType())) {
				return !characterService.isPossess(infos.getItemCode(), 2, bankDAO);
			} else {
				return !characterService.isPossess(infos.getItemCode(), bankDAO);
			}
		}, maxLevel);
	}

	private List<ArtifactGoalAchiever> searchGearGoal(BotCharacter character, List<GoalAchieverInfo> allGoals,
			List<GoalAchieverInfo> allSimulateGoals, int maxLevel) {
		int craftingLevel = character.getGearcraftingLevel();
		BotCraftSkill craftSkill = BotCraftSkill.GEARCRAFTING;
		return searchGoal(character, allGoals, allSimulateGoals, craftingLevel, craftSkill,
				infos -> !characterService.isPossess(infos.getItemCode(), bankDAO), maxLevel);
	}

	private List<ArtifactGoalAchiever> searchWeaponGoal(BotCharacter character, List<GoalAchieverInfo> allGoals,
			List<GoalAchieverInfo> allSimulateGoals, int maxLevel) {
		int craftingLevel = character.getWeaponcraftingLevel();
		BotCraftSkill craftSkill = BotCraftSkill.WEAPONCRAFTING;
		return searchGoal(character, allGoals, allSimulateGoals, craftingLevel, craftSkill,
				infos -> !characterService.isPossess(infos.getItemCode(), bankDAO), maxLevel);
	}

	private List<ArtifactGoalAchiever> searchGoal(BotCharacter character, List<GoalAchieverInfo> allGoals,
			List<GoalAchieverInfo> allSimulateGoals, int craftingLevel, BotCraftSkill craftSkill,
			Predicate<GoalAchieverInfo> predicateFilterPossesed, int maxLevel) {
		if (craftingLevel == GameConstants.MAX_LEVEL) {
			int minSkillLevel = craftingLevel - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP;
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, craftingLevel + 1);
			Predicate<GoalAchieverInfo> predicate = StrategySkillUtils.createFilterCraftPredicate(craftSkill, bornes);
			return allGoals.stream().filter(predicate).filter(predicateFilterPossesed).map(GoalAchieverInfo::getGoal)
					.toList();
		}
		if (craftingLevel % GameConstants.STEP_LEVEL == 0) {
			Bornes bornes = new Bornes(craftingLevel, craftingLevel, craftingLevel + 1);
			Predicate<GoalAchieverInfo> predicate = StrategySkillUtils.createFilterCraftPredicate(craftSkill, bornes);
			List<ArtifactGoalAchiever> resultList = allGoals.stream().filter(predicate).filter(predicateFilterPossesed)
					.map(GoalAchieverInfo::getGoal).filter(aga -> aga.isRealisableAfterSetRoot(character)).toList();
			if (!resultList.isEmpty()) {
				return resultList;
			}
		}
		int minSkillLevel = Math.max(1, craftingLevel - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
		Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, craftingLevel + 1);
		// On filtre le plus rapide??
		int minTime = Integer.MAX_VALUE;
		String goalCode = "";
		Predicate<GoalAchieverInfo> simulatedPredicate = StrategySkillUtils.createFilterCraftPredicate(craftSkill,
				bornes);
		List<GoalAchieverInfo> simGoals = allSimulateGoals.stream().filter(simulatedPredicate).toList();
		List<? extends BotItemReader> bankItems = bankDAO.viewItems();
		SumAccumulator accumulator = new SumAccumulator();
		simulatorManager.getSimulatorListener()
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));
		for (GoalAchieverInfo simGoal : simGoals) {
			boolean success = true;
			optimize(simGoal);
			accumulator.reset();
			try {
				for (int i = 0; i < NUMBER_OF_SIMULATE; i++) {
					simulatorManager.setValue(character, bankItems);
					if (simGoal.getGoal().isRealisableAfterSetRoot(character)) {
						simGoal.getGoal().clear();
						if (!simGoal.getGoal().execute(new HashMap<>())) {
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
					goalCode = simGoal.getItemCode();
				}
			} catch (StopSimulationException sse) {
				// Arrêt de la simulation
			}
		}
		String goalCodeFound = goalCode;
		Optional<GoalAchieverInfo> searchRealGoal = allGoals.stream()
				.filter(aga -> aga.getItemCode().equals(goalCodeFound)).findFirst();
		List<ArtifactGoalAchiever> result = new ArrayList<>();
		if (searchRealGoal.isPresent()) {
			optimize(searchRealGoal.get());
			ArtifactGoalAchiever itemRecycleGoalAchiever = goalFactory.addItemRecycleGoalAchiever(searchRealGoal.get(),
					Strategy.calculMinItemPreserve(searchRealGoal.get()));
			result.add(itemRecycleGoalAchiever);
			if (craftingLevel < maxLevel) {
				result.add(itemRecycleGoalAchiever);
			}
		}
		return result;
	}

	private ArtifactGoalAchiever searchFishingGoal(BotCharacter character, List<GoalAchieverInfo> allGoals) {
		if (character.getFishingLevel() < GameConstants.MAX_LEVEL) {
			int minSkillLevel = Math.max(1, character.getFishingLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getFishingLevel() + 1);
			Predicate<GoalAchieverInfo> predicate = StrategySkillUtils.createFilterResourcePredicate(bornes,
					BotResourceSkill.FISHING);

			Optional<GoalAchieverInfo> resultGoal = allGoals.stream().filter(predicate)
					.max((aga1, aga2) -> Integer.compare(aga1.getLevel(), aga2.getLevel()));
			if (resultGoal.isPresent()) {
				GoalAchieverLoop returnGoal = new GoalAchieverLoop(resultGoal.get().getGoal(), 10, true);
				if (characterService.isPossessAny(itemService.getToolsCode(BotResourceSkill.FISHING), bankDAO)) {
					optimize(returnGoal);
				}
				return returnGoal;
			}
		}
		return null;
	}

	private ArtifactGoalAchiever searchWoodcuttingGoal(BotCharacter character, List<GoalAchieverInfo> allGoals) {
		if (character.getWoodcuttingLevel() < GameConstants.EVENT_RESOURCE_LEVEL) {
			int minSkillLevel = Math.max(1,
					character.getWoodcuttingLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getWoodcuttingLevel() + 1);
			Predicate<GoalAchieverInfo> predicate = StrategySkillUtils
					.createFilterCraftPredicate(BotCraftSkill.WOODCUTTING, bornes);
			Optional<GoalAchieverInfo> resultGoal = allGoals.stream().filter(predicate)
					.max((aga1, aga2) -> Integer.compare(aga1.getLevel(), aga2.getLevel()));
			if (resultGoal.isPresent()) {
				if (characterService.isPossessAny(itemService.getToolsCode(BotResourceSkill.WOODCUTTING), bankDAO)) {
					GoalAchieverLoop returnGoal = new GoalAchieverLoop(resultGoal.get().getGoal(), 5, true);
					optimize(returnGoal);
					return returnGoal;
				}
				return resultGoal.get().getGoal();
			}
		}
		return null;
	}

	private ArtifactGoalAchiever searchMiningGoal(BotCharacter character, List<GoalAchieverInfo> allGoals) {
		if (character.getMiningLevel() < GameConstants.EVENT_RESOURCE_LEVEL) {
			int minSkillLevel = Math.max(1, character.getMiningLevel() - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, character.getMiningLevel() + 1);
			Predicate<GoalAchieverInfo> predicate = StrategySkillUtils.createFilterCraftPredicate(BotCraftSkill.MINING,
					bornes);
			Optional<GoalAchieverInfo> resultGoal = allGoals.stream().filter(predicate)
					.max((aga1, aga2) -> Integer.compare(aga1.getLevel(), aga2.getLevel()));
			if (resultGoal.isPresent()) {
				if (characterService.isPossessAny(itemService.getToolsCode(BotResourceSkill.MINING), bankDAO)) {
					GoalAchieverLoop returnGoal = new GoalAchieverLoop(resultGoal.get().getGoal(), 5, true);
					optimize(returnGoal);
					return returnGoal;
				}
				return resultGoal.get().getGoal();
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
		return Strategy.isAcceptEvent(characterDAO, type, code, monsterGoalsForEvent, itemGoalsForEvent);
	}

	@Override
	public void initializeGoal(String type, String code) {
		eventGoal = Strategy.initializeGoal(goalFactory, type, code, monsterGoalsForEvent, itemGoalsForEvent);

	}

	@Override
	public GoalAchiever getEventGoalAchiever() {
		return eventGoal;
	}

	private void optimize(GoalAchieverInfo goalAchiever) {
		if (!goalAchiever.isNeedTaskMasterResource() && !goalAchiever.isNeedRareResource()) {
			optimize(goalAchiever.getGoal());
		} else {
			goalAverageOptimizer.optimize(goalAchiever.getGoal(), 1, 1f);
		}
	}

	private void optimize(ArtifactGoalAchiever goalAchiever) {
		goalAverageOptimizer.optimize(goalAchiever, MAX_MULTIPLIER_COEFFICIENT, 0.9f);
	}
}
