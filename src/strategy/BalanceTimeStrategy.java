package strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.TimeGoalAchiever;
import strategy.achiever.TimeGoalAchiever.XpGetter;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.GoalFactory.GoalFilter;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.goals.MonsterItemDropGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.info.GoalAchieverInfo.INFO_TYPE;
import strategy.util.AverageTimeXpCalculator;
import strategy.util.Bornes;
import strategy.util.CharacterService;
import strategy.util.StrategySkillUtils;

public final class BalanceTimeStrategy implements Strategy {

	private static final int AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE = 10;
	private final CharacterDAO characterDAO;
	private final List<GoalAchiever> inventoryGoals;
	private final Collection<GoalAchieverInfo<ArtifactGoalAchiever>> itemGoals;
	private final List<GoalAchiever> taskGoals;
	private final Map<String, AverageTimeXpCalculator> timeGoalAchieverMap;
	private final List<MonsterGoalAchiever> monsterGoals;
	private int currentCall;
	private static final int[][] SKILL_LEVEL_LIST = new int[][] { Strategy.FISHING_LEVELS, Strategy.COOKING_LEVELS,
			Strategy.WOODCUTTING_LEVELS, Strategy.GEARCRAFTING_LEVELS, Strategy.MINING_LEVELS,
			Strategy.WEAPONCRAFTING_LEVELS, Strategy.JEWELRYCRAFTING_LEVELS, Strategy.ALCHEMY_LEVELS };
	private final List<XpGetter> xpGetters;
	private List<GoalAchieverInfo<MonsterItemDropGoalAchiever>> dropItemGoal;
	private final CharacterService characterService;
	private final GoalFactory goalFactory;
	private GoalAchiever eventGoal;
	private final BankDAO bankDAO;
	private final List<MonsterGoalAchiever> monsterGoalsForEvent;
	private final Collection<GoalAchieverInfo<ArtifactGoalAchiever>> itemGoalsForEvent;

	public BalanceTimeStrategy(CharacterDAO characterDAO, GoalFactory goalFactory, CharacterService characterService,
			BankDAO bankDAO) {
		this.characterDAO = characterDAO;
		this.goalFactory = goalFactory;
		this.characterService = characterService;
		this.bankDAO = bankDAO;
		itemGoals = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING,
				GoalFilter.NO_EVENT);
		itemGoalsForEvent = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.GATHERING, GoalFilter.EVENT);
		inventoryGoals = goalFactory.createManagedInventoryCustomGoal();
		monsterGoals = goalFactory.createMonstersGoals(resp -> resp.fight().getXp() == 0, GoalFilter.NO_EVENT);
		monsterGoalsForEvent = goalFactory.createMonstersGoals(resp -> false, GoalFilter.EVENT);
		taskGoals = goalFactory.createTaskGoals();
		timeGoalAchieverMap = new HashMap<>();
		itemGoals.stream().forEach(ga -> timeGoalAchieverMap.put(ga.getItemCode(),
				new AverageTimeXpCalculator(AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE)));
		monsterGoals.stream().forEach(ga -> timeGoalAchieverMap.put(ga.getMonsterCode(),
				new AverageTimeXpCalculator(AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE)));
		currentCall = 1;
		dropItemGoal = goalFactory.createDropItemGoal();
		xpGetters = new ArrayList<>(SKILL_LEVEL_LIST.length);
		xpGetters.add(() -> characterDAO.getCharacter().getFishingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getCookingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getWoodcuttingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getGearcraftingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getMiningXp());
		xpGetters.add(() -> characterDAO.getCharacter().getWeaponcraftingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getJewelrycraftingXp());
		xpGetters.add(() -> characterDAO.getCharacter().getAlchemyXp());
	}

	@Override
	public Deque<GoalAchiever> getGoalAchievers() {
		BotCharacter character = this.characterDAO.getCharacter();
		int[] skillLevels = new int[] { character.getFishingLevel(), character.getCookingLevel(),
				character.getWoodcuttingLevel(), character.getGearcraftingLevel(), character.getMiningLevel(),
				character.getWeaponcraftingLevel(), character.getJewelrycraftingLevel(), character.getAlchemyLevel() };
		List<GoalAchieverInfo<ArtifactGoalAchiever>> allGoals = Strategy.filterTaskGoals(itemGoals, characterService, bankDAO);
		// search min skill
		int index = StrategySkillUtils.getMinSkillIndex(skillLevels);
		Deque<GoalAchiever> goalAchievers = new LinkedList<>();
		if (skillLevels[index] < GameConstants.MAX_SKILL_LEVEL) {
			// recherche tous les buts pour augmenter le skillMin
			Bornes bornes = StrategySkillUtils.getBorneLevel(skillLevels[index], SKILL_LEVEL_LIST[index]);
			bornes = new Bornes(bornes.oldMin(), bornes.oldMin(), bornes.max());
			List<Predicate<GoalAchieverInfo<ArtifactGoalAchiever>>> filterPredicate = new ArrayList<>(SKILL_LEVEL_LIST.length);
			filterPredicate.addAll(BalanceRateStrategy.createFiltersPredicate(bornes));
			List<GoalAchieverInfo<ArtifactGoalAchiever>> searchGoalAchievers = allGoals.stream().filter(filterPredicate.get(index))
					.sorted((c1, c2) -> Double.compare(timeGoalAchieverMap.get(c1.getItemCode()).getAverage(),
							timeGoalAchieverMap.get(c2.getItemCode()).getAverage()))
					.toList().reversed();
			Optional<GoalAchieverInfo<ArtifactGoalAchiever>> goalAchiever = searchGoalAchievers.stream()
					.filter(ga -> ga.getGoal().isRealisableAfterSetRoot(character)).findFirst();
			float nbGoalNeedTask = searchGoalAchievers.stream()
					.<Float>map(aga -> aga.isNeedTaskMasterResource() ? 1f : 0f).reduce(0f, (a, b) -> a + b);
			int maxTurn = Math.round((50f - 49f * nbGoalNeedTask / searchGoalAchievers.size()));
			currentCall = (currentCall + 1) % maxTurn;
			if (goalAchiever.isPresent()) {
				goalAchievers.add(createGoalAchiever(goalAchiever.get(), xpGetters.get(index)));
				if (currentCall == 0) {
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
				goalAchievers.addAll(taskGoals);
				goalAchievers.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, bankDAO));
			}
			return goalAchievers;
		}

		// On craft que du niveau max
		goalAchievers.addAll(allGoals.stream()
				.filter(ga -> ga.isCraft() && ga.isLevel(GameConstants.MAX_SKILL_LEVEL, INFO_TYPE.CRAFTING))
				.map(GoalAchieverInfo::getGoal).toList());
		goalAchievers.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, bankDAO));
		goalAchievers.addAll(taskGoals);
		return goalAchievers;
	}

	private GoalAchiever createGoalAchiever(GoalAchieverInfo<ArtifactGoalAchiever> goalAchiever, XpGetter xpGetter) {
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
