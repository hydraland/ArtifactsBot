package strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotEffect;
import hydra.model.BotResourceSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.TimeGoalAchiever;
import strategy.achiever.TimeGoalAchiever.XpGetter;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.GoalFactory.GoalFilter;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.info.GoalAchieverInfo.INFO_TYPE;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.util.AverageTimeXpCalculator;
import strategy.util.Bornes;
import strategy.util.CharacterService;
import strategy.util.StrategySkillUtils;

public final class OptimisedTimeStrategy implements Strategy {

	private static final int AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE = 10;
	private static final int ITEM_INITIAL_AVERAGE_TIME_VALUE = 1000;
	private final CharacterDAO characterDAO;
	private final List<GoalAchiever> inventoryGoals;
	private final Collection<GoalAchieverInfo> itemGoals;
	private final List<GoalAchiever> taskGoals;
	private final Map<String, AverageTimeXpCalculator> timeGoalAchieverMap;
	private final List<MonsterGoalAchiever> monsterGoals;
	private int currentCall;
	private final List<XpGetter> xpGetters;
	private final List<GoalAchieverInfo> dropItemGoal;
	private final CharacterService characterService;
	private final GoalFactory goalFactory;
	private GoalAchiever eventGoal;
	private final BankDAO bankDAO;
	private final List<MonsterGoalAchiever> monsterGoalsForEvent;
	private final GoalAverageOptimizer goalAverageOptimizer;
	private final Collection<GoalAchieverInfo> itemGoalsForEvent;

	public OptimisedTimeStrategy(CharacterDAO characterDAO, ItemDAO itemDao, GoalFactory goalFactory,
			CharacterService characterService, BankDAO bankDAO, GoalAverageOptimizer goalAverageOptimizer) {
		this.characterDAO = characterDAO;
		this.goalFactory = goalFactory;
		this.characterService = characterService;
		this.bankDAO = bankDAO;
		this.goalAverageOptimizer = goalAverageOptimizer;
		itemGoals = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING,
				GoalFilter.NO_EVENT);
		itemGoalsForEvent = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.GATHERING, GoalFilter.EVENT);
		inventoryGoals = goalFactory.createManagedInventoryCustomGoal();
		monsterGoals = goalFactory.createMonstersGoals(resp -> resp.fight().getXp() == 0, GoalFilter.NO_EVENT);
		monsterGoalsForEvent = goalFactory.createMonstersGoals(resp -> false, GoalFilter.EVENT);
		taskGoals = goalFactory.createTaskGoals();
		timeGoalAchieverMap = new HashMap<>();
		dropItemGoal = goalFactory.getDropItemGoal();
		List<String> itemWithHasteEffectListCode = itemDao.getItems().stream()
				.filter(bid -> bid.getEffects().stream().anyMatch(bie -> BotEffect.HASTE.equals(bie.getName())))
				.map(bid -> bid.getCode()).toList();
		itemGoals.stream().forEach(ga -> {
			AverageTimeXpCalculator averageTimeXpCalculator;
			String code = ga.getItemCode();
			if (itemWithHasteEffectListCode.contains(code)) {
				averageTimeXpCalculator = new AverageTimeXpCalculator(ITEM_INITIAL_AVERAGE_TIME_VALUE + 1,
						AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE);
			} else {
				averageTimeXpCalculator = new AverageTimeXpCalculator(ITEM_INITIAL_AVERAGE_TIME_VALUE,
						AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE);
			}
			timeGoalAchieverMap.put(code, averageTimeXpCalculator);
		});
		monsterGoals.stream().forEach(ga -> timeGoalAchieverMap.put(ga.getMonsterCode(),
				new AverageTimeXpCalculator(AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE)));
		currentCall = 1;
		xpGetters = new ArrayList<>();
		xpGetters.add(() -> characterDAO.getCharacter().getGearcraftingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getWeaponcraftingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getJewelrycraftingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getCookingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getWoodcuttingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getMiningXp());
		xpGetters.add(() -> characterDAO.getCharacter().getAlchemyXp());
		xpGetters.add(() -> characterDAO.getCharacter().getFishingXp());
	}

	@Override
	public Iterable<GoalAchiever> getGoalAchievers() {
		BotCharacter character = this.characterDAO.getCharacter();
		int[] skillLevels = new int[] { character.getGearcraftingLevel(), character.getWeaponcraftingLevel(),
				character.getJewelrycraftingLevel(), character.getCookingLevel(), character.getWoodcuttingLevel(),
				character.getMiningLevel(), character.getAlchemyLevel(), character.getFishingLevel() };
		List<GoalAchieverInfo> allGoals = Strategy.filterTaskGoals(itemGoals, characterService, bankDAO);
		// search min skill
		int index = StrategySkillUtils.getMinSkillIndex(skillLevels);
		if (skillLevels[index] < GameConstants.MAX_SKILL_LEVEL) {
			// recherche tous les buts pour augmenter le skillMin
			int minSkillLevel = Math.max(1, skillLevels[index] - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP);
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, skillLevels[index]);
			List<Predicate<GoalAchieverInfo>> filterPredicate = new ArrayList<>();
			filterPredicate.addAll(createFiltersPredicate(bornes));
			List<GoalAchieverInfo> searchGoalAchievers = allGoals.stream().filter(filterPredicate.get(index))
					.sorted((c1, c2) -> Double.compare(timeGoalAchieverMap.get(c1.getItemCode()).getAverage(),
							timeGoalAchieverMap.get(c2.getItemCode()).getAverage()))
					.toList().reversed();
			ArrayList<GoalAchiever> goalAchievers = new ArrayList<>();
			Optional<GoalAchieverInfo> goalAchiever = searchGoalAchievers.stream()
					.filter(ga -> ga.getGoal().isRealisableAfterSetRoot(character)).findFirst();
			float nbGoalNeedTask = searchGoalAchievers.stream()
					.<Float>map(aga -> aga.isNeedTaskMasterResource() ? 1f : 0f).reduce(0f, (a, b) -> a + b);
			int maxTurn = Math.round((3f - 2f * nbGoalNeedTask / searchGoalAchievers.size()));
			currentCall = (currentCall + 1) % maxTurn;
			if (goalAchiever.isPresent()) {
				goalAchievers.add(createGoalAchiever(goalAchiever.get(), xpGetters.get(index)));
				// Pour optimiser le temps on ne fait les autres tâches que si on craft
				if (currentCall == 0 && goalAchiever.get().isCraft()) {
					goalAchievers.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, bankDAO));
					goalAchievers.addAll(taskGoals);
				}
			} else {
				int charLevel = character.getLevel();
				List<MonsterGoalAchiever> monstersGoal = monsterGoals.stream()
						.filter(mga -> mga.getMonsterLevel() <= charLevel
								&& mga.getMonsterLevel() >= (charLevel - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP))
						.sorted((c1, c2) -> Double.compare(timeGoalAchieverMap.get(c1.getMonsterCode()).getAverage(),
								timeGoalAchieverMap.get(c2.getMonsterCode()).getAverage()))
						.toList().reversed();
				Optional<MonsterGoalAchiever> monsterGoalAchiever = monstersGoal.stream()
						.filter(ga -> ga.isRealisableAfterSetRoot(character)).findFirst();
				goalAchievers.add(createGoalAchiever(monsterGoalAchiever.get(), xpGetters.get(index)));
				goalAchievers.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, bankDAO));
				goalAchievers.addAll(taskGoals);
			}
			return goalAchievers;
		}

		// On craft que du niveau max
		ArrayList<GoalAchiever> goalAchievers = new ArrayList<>();
		goalAchievers.addAll(allGoals.stream()
				.filter(ga -> ga.isCraft() && ga.isLevel(GameConstants.MAX_SKILL_LEVEL, INFO_TYPE.CRAFTING))
				.map(GoalAchieverInfo::getGoal).toList());
		goalAchievers.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, bankDAO));
		goalAchievers.addAll(taskGoals);
		return goalAchievers;
	}

	private GoalAchiever createGoalAchiever(GoalAchieverInfo goalAchiever, XpGetter xpGetter) {
		optimize(goalAchiever);
		BotCraftSkill botCraftSkill = goalAchiever.getBotCraftSkill();
		if (goalAchiever.isCraft() && (botCraftSkill.equals(BotCraftSkill.WEAPONCRAFTING)
				|| botCraftSkill.equals(BotCraftSkill.GEARCRAFTING)
				|| botCraftSkill.equals(BotCraftSkill.JEWELRYCRAFTING))) {
			GoalAchiever goalAchieverWithRecycle = goalFactory.addItemRecycleGoalAchiever(goalAchiever,
					Strategy.calculMinItemPreserve(goalAchiever));
			return new TimeGoalAchiever(goalAchieverWithRecycle, xpGetter,
					timeGoalAchieverMap.get(goalAchiever.getItemCode()), true);
		} else {
			return new TimeGoalAchiever(goalAchiever.getGoal(), xpGetter,
					timeGoalAchieverMap.get(goalAchiever.getItemCode()), true);
		}
	}

	private void optimize(GoalAchieverInfo goalAchiever) {
		if (timeGoalAchieverMap.get(goalAchiever.getItemCode()).getAverage() < ITEM_INITIAL_AVERAGE_TIME_VALUE
				&& !goalAchiever.isNeedTaskMasterResource() && !goalAchiever.isNeedRareResource()) {
			goalAverageOptimizer.optimize(goalAchiever.getGoal(), Integer.MAX_VALUE, 0.9f);
		} else {
			goalAverageOptimizer.optimize(goalAchiever.getGoal(), 1, 1f);
		}
	}

	static List<Predicate<GoalAchieverInfo>> createFiltersPredicate(Bornes bornes) {
		ArrayList<Predicate<GoalAchieverInfo>> filterPredicate = new ArrayList<>();
		filterPredicate.add(StrategySkillUtils.createFilterCraftPredicate(BotCraftSkill.GEARCRAFTING, bornes));
		filterPredicate.add(StrategySkillUtils.createFilterCraftPredicate(BotCraftSkill.WEAPONCRAFTING, bornes));
		filterPredicate.add(StrategySkillUtils.createFilterCraftPredicate(BotCraftSkill.JEWELRYCRAFTING, bornes));
		filterPredicate.add(StrategySkillUtils.createFilterCraftPredicate(BotCraftSkill.COOKING, bornes));
		filterPredicate.add(StrategySkillUtils.createFilterResourceAndCraftPredicate(bornes,
				BotResourceSkill.WOODCUTTING, BotCraftSkill.WOODCUTTING));
		filterPredicate.add(StrategySkillUtils.createFilterResourceAndCraftPredicate(bornes, BotResourceSkill.MINING,
				BotCraftSkill.MINING));
		filterPredicate.add(StrategySkillUtils.createFilterResourceAndCraftPredicate(bornes, BotResourceSkill.ALCHEMY,
				BotCraftSkill.ALCHEMY));
		filterPredicate.add(StrategySkillUtils.createFilterResourcePredicate(bornes, BotResourceSkill.FISHING));
		return filterPredicate;
	}

	private GoalAchiever createGoalAchiever(MonsterGoalAchiever goalAchiever, XpGetter xpGetter) {
		return new TimeGoalAchiever(goalAchiever, xpGetter, timeGoalAchieverMap.get(goalAchiever.getMonsterCode()),
				true);
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
}