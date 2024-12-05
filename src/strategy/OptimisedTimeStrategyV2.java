package strategy;

import java.util.ArrayList;
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
import strategy.achiever.GoalAchiever;
import strategy.achiever.TimeGoalAchiever;
import strategy.achiever.TimeGoalAchiever.XpGetter;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo.INFO_TYPE;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.util.AverageTimeXpCalculator;
import strategy.util.Bornes;
import strategy.util.CharacterService;
import strategy.util.StrategySkillUtils;

public final class OptimisedTimeStrategyV2 implements Strategy {

	private static final int MAX_MULTIPLIER_COEFFICIENT = 5;
	private static final int AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE = 3;
	private static final int ITEM_INITIAL_AVERAGE_TIME_VALUE = 1000;
	private final CharacterDAO characterDAO;
	private final List<GoalAchiever> inventoryGoals;
	private final List<ArtifactGoalAchiever> itemGoals;
	private final List<GoalAchiever> taskGoals;
	private final Map<String, AverageTimeXpCalculator> timeGoalAchieverMap;
	private final List<MonsterGoalAchiever> monsterGoals;
	private int currentCall;
	private final List<XpGetter> xpGetters;
	private final List<ArtifactGoalAchiever> dropItemGoal;
	private final CharacterService characterService;
	private final GoalFactory goalFactory;
	private GoalAchiever eventGoal;
	private final BankDAO bankDAO;
	private final List<MonsterGoalAchiever> monsterGoalsForEvent;
	private final GoalAverageOptimizer goalAverageOptimizer;

	public OptimisedTimeStrategyV2(CharacterDAO characterDAO, ItemDAO itemDao, GoalFactory goalFactory,
			CharacterService characterService, BankDAO bankDAO,GoalAverageOptimizer goalAverageOptimizer) {
		this.characterDAO = characterDAO;
		this.goalFactory = goalFactory;
		this.characterService = characterService;
		this.bankDAO = bankDAO;
		this.goalAverageOptimizer = goalAverageOptimizer;
		itemGoals = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING);
		inventoryGoals = goalFactory.createManagedInventoryCustomGoal();
		monsterGoals = goalFactory.createMonstersGoals(resp -> resp.fight().getXp() == 0);
		monsterGoalsForEvent = goalFactory.createMonstersGoals(resp -> false);
		taskGoals = goalFactory.createTaskGoals();
		timeGoalAchieverMap = new HashMap<>();
		dropItemGoal = goalFactory.getDropItemGoal();
		List<String> itemWithHasteEffectListCode = itemDao.getItems().stream()
				.filter(bid -> bid.getEffects().stream().anyMatch(bie -> BotEffect.HASTE.equals(bie.getName())))
				.map(bid -> bid.getCode()).toList();
		itemGoals.stream().forEach(ga -> {
			AverageTimeXpCalculator averageTimeXpCalculator;
			String code = goalFactory.getInfos(ga).getItemCode();
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
		xpGetters.add(() -> characterDAO.getCharacter().getWoodcuttingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getMiningXp());
	}

	@Override
	public Iterable<GoalAchiever> getGoalAchievers() {
		BotCharacter character = this.characterDAO.getCharacter();
		int[] skillLevels = new int[] { character.getGearcraftingLevel(), character.getWeaponcraftingLevel(),
				character.getJewelrycraftingLevel(), character.getWoodcuttingLevel(), character.getMiningLevel() };
		List<ArtifactGoalAchiever> allGoals = Strategy.filterTaskGoals(itemGoals, characterService, goalFactory,
				bankDAO);
		// search min skill
		int index = StrategySkillUtils.getMinSkillIndex(skillLevels);
		if (skillLevels[index] < GameConstants.MAX_SKILL_LEVEL) {
			// recherche tous les buts pour augmenter le skillMin
			int minSkillLevel = Math.max(1, skillLevels[index] - GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP + 1);
			Bornes bornes = new Bornes(minSkillLevel, minSkillLevel, skillLevels[index]);
			List<Predicate<ArtifactGoalAchiever>> filterPredicate = new ArrayList<>();
			filterPredicate.addAll(createFiltersPredicate(goalFactory, bornes));
			List<ArtifactGoalAchiever> searchGoalAchievers = allGoals.stream().filter(filterPredicate.get(index))
					.sorted((c1, c2) -> Double.compare(
							timeGoalAchieverMap.get(goalFactory.getInfos(c1).getItemCode()).getAverage(),
							timeGoalAchieverMap.get(goalFactory.getInfos(c2).getItemCode()).getAverage()))
					.toList().reversed();
			ArrayList<GoalAchiever> goalAchievers = new ArrayList<>();
			Optional<ArtifactGoalAchiever> goalAchiever = searchGoalAchievers.stream()
					.filter(ga -> ga.isRealisableAfterSetRoot(character)).findFirst();
			float nbGoalNeedTask = searchGoalAchievers.stream()
					.<Float>map(aga -> goalFactory.getInfos(aga).isNeedTaskMasterResource() ? 1f : 0f)
					.reduce(0f, (a, b) -> a + b);
			int maxTurn = Math.round((5f - 4f * nbGoalNeedTask / searchGoalAchievers.size()));
			currentCall = (currentCall + 1) % maxTurn;
			if (goalAchiever.isPresent()) {
				goalAchievers.add(createGoalAchiever(goalAchiever.get(), xpGetters.get(index)));
				// Pour optimiser le temps on ne fait les autres tâches que si on craft
				if (currentCall == 0 && goalFactory.getInfos(goalAchiever.get()).isCraft()) {
					goalAchievers
							.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, goalFactory, bankDAO));
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
				goalAchievers
						.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, goalFactory, bankDAO));
				goalAchievers.addAll(taskGoals);
			}
			return goalAchievers;
		}

		// On craft que du niveau max
		ArrayList<GoalAchiever> goalAchievers = new ArrayList<>();
		goalAchievers.addAll(allGoals.stream()
				.filter(ga -> goalFactory.getInfos(ga).isCraft()
						&& goalFactory.getInfos(ga).isLevel(GameConstants.MAX_SKILL_LEVEL, INFO_TYPE.CRAFTING))
				.toList());
		goalAchievers.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, goalFactory, bankDAO));
		goalAchievers.addAll(taskGoals);
		return goalAchievers;
	}

	private GoalAchiever createGoalAchiever(ArtifactGoalAchiever goalAchiever, XpGetter xpGetter) {
		optimize(goalAchiever);
		BotCraftSkill botCraftSkill = goalFactory.getInfos(goalAchiever).getBotCraftSkill();
		if (goalFactory.getInfos(goalAchiever).isCraft() && (botCraftSkill.equals(BotCraftSkill.WEAPONCRAFTING)
				|| botCraftSkill.equals(BotCraftSkill.GEARCRAFTING)
				|| botCraftSkill.equals(BotCraftSkill.JEWELRYCRAFTING))) {
			GoalAchiever goalAchieverWithRecycle = goalFactory.addItemRecycleGoalAchiever(goalAchiever,
					Strategy.calculMinItemPreserve(goalFactory, goalAchiever));
			return new TimeGoalAchiever(goalAchieverWithRecycle, xpGetter,
					timeGoalAchieverMap.get(goalFactory.getInfos(goalAchiever).getItemCode()), true);
		} else {
			return new TimeGoalAchiever(goalAchiever, xpGetter,
					timeGoalAchieverMap.get(goalFactory.getInfos(goalAchiever).getItemCode()), true);
		}
	}

	private void optimize(ArtifactGoalAchiever goalAchiever) {
		if (timeGoalAchieverMap.get(goalFactory.getInfos(goalAchiever).getItemCode())
				.getAverage() < ITEM_INITIAL_AVERAGE_TIME_VALUE
				&& !goalFactory.getInfos(goalAchiever).isNeedTaskMasterResource()
				&& !goalFactory.getInfos(goalAchiever).isNeedRareResource()) {
			goalAverageOptimizer.optimize(goalAchiever, MAX_MULTIPLIER_COEFFICIENT, 0.9f);
		} else {
			goalAverageOptimizer.optimize(goalAchiever, 1, 1f);
		}
	}

	static List<Predicate<ArtifactGoalAchiever>> createFiltersPredicate(GoalFactory goalFactory, Bornes bornes) {
		ArrayList<Predicate<ArtifactGoalAchiever>> filterPredicate = new ArrayList<>();
		filterPredicate
				.add(StrategySkillUtils.createFilterCraftPredicate(goalFactory, BotCraftSkill.GEARCRAFTING, bornes));
		filterPredicate
				.add(StrategySkillUtils.createFilterCraftPredicate(goalFactory, BotCraftSkill.WEAPONCRAFTING, bornes));
		filterPredicate
				.add(StrategySkillUtils.createFilterCraftPredicate(goalFactory, BotCraftSkill.JEWELRYCRAFTING, bornes));
		filterPredicate
				.add(StrategySkillUtils.createFilterCraftPredicate(goalFactory, BotCraftSkill.WOODCUTTING, bornes));
		filterPredicate.add(StrategySkillUtils.createFilterCraftPredicate(goalFactory, BotCraftSkill.MINING, bornes));
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
}
