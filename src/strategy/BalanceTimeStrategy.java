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
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.TimeGoalAchiever;
import strategy.achiever.TimeGoalAchiever.XpGetter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.info.GoalAchieverInfo.INFO_TYPE;
import strategy.achiever.factory.GoalFactory;
import strategy.util.AverageTimeXpCalculator;
import strategy.util.Bornes;
import strategy.util.CharacterService;
import strategy.util.StrategySkillUtils;

public final class BalanceTimeStrategy implements Strategy {

	private static final int AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE = 10;
	private final CharacterDAO characterDAO;
	private final List<GoalAchiever> inventoryGoals;
	private final List<ArtifactGoalAchiever> itemGoals;
	private final List<GoalAchiever> taskGoals;
	private final Map<String, AverageTimeXpCalculator> timeGoalAchieverMap;
	private final List<MonsterGoalAchiever> monsterGoals;
	private int currentCall;
	private static final int[][] SKILL_LEVEL_LIST = new int[][] { Strategy.FISHING_LEVELS, Strategy.COOKING_LEVELS,
			Strategy.WOODCUTTING_LEVELS, Strategy.GEARCRAFTING_LEVELS, Strategy.MINING_LEVELS,
			Strategy.WEAPONCRAFTING_LEVELS, Strategy.JEWELRYCRAFTING_LEVELS, Strategy.ALCHEMY_LEVELS };
	private final List<XpGetter> xpGetters;
	private List<ArtifactGoalAchiever> dropItemGoal;
	private final CharacterService characterService;
	private final GoalFactory goalFactory;
	private GoalAchiever eventGoal;
	private final BankDAO bankDAO;
	private final List<MonsterGoalAchiever> monsterGoalsForEvent;

	public BalanceTimeStrategy(CharacterDAO characterDAO, GoalFactory goalFactory, CharacterService characterService,
			BankDAO bankDAO) {
		this.characterDAO = characterDAO;
		this.goalFactory = goalFactory;
		this.characterService = characterService;
		this.bankDAO = bankDAO;
		itemGoals = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING_AND_GATHERING);
		inventoryGoals = goalFactory.createManagedInventoryCustomGoal();
		monsterGoals = goalFactory.createMonstersGoals(resp -> resp.fight().getXp() == 0);
		monsterGoalsForEvent = goalFactory.createMonstersGoals(resp -> false);
		taskGoals = goalFactory.createTaskGoals();
		timeGoalAchieverMap = new HashMap<>();
		itemGoals.stream().forEach(ga -> {
			timeGoalAchieverMap.put(goalFactory.getInfos(ga).getItemCode(),
					new AverageTimeXpCalculator(AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE));
		});
		monsterGoals.stream().forEach(ga -> timeGoalAchieverMap.put(ga.getMonsterCode(),
				new AverageTimeXpCalculator(AVERAGE_TIME_XP_CALCULATOR_MAX_SIZE)));
		currentCall = 1;
		dropItemGoal = goalFactory.getDropItemGoal();
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
	public Iterable<GoalAchiever> getGoalAchievers() {
		BotCharacter character = this.characterDAO.getCharacter();
		int[] skillLevels = new int[] { character.getFishingLevel(), character.getCookingLevel(),
				character.getWoodcuttingLevel(), character.getGearcraftingLevel(), character.getMiningLevel(),
				character.getWeaponcraftingLevel(), character.getJewelrycraftingLevel(), character.getAlchemyLevel() };
		List<ArtifactGoalAchiever> allGoals = Strategy.filterTaskGoals(itemGoals, characterService, goalFactory,
				bankDAO);
		// search min skill
		int index = StrategySkillUtils.getMinSkillIndex(skillLevels);
		if (skillLevels[index] < GameConstants.MAX_SKILL_LEVEL) {
			// recherche tous les buts pour augmenter le skillMin
			Bornes bornes = StrategySkillUtils.getBorneLevel(skillLevels[index], SKILL_LEVEL_LIST[index]);
			bornes = new Bornes(bornes.oldMin(), bornes.oldMin(), bornes.max());
			List<Predicate<ArtifactGoalAchiever>> filterPredicate = new ArrayList<>(SKILL_LEVEL_LIST.length);
			filterPredicate.addAll(BalanceRateStrategy.createFiltersPredicate(goalFactory, bornes));
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
			int maxTurn = Math.round((50f - 49f * nbGoalNeedTask / searchGoalAchievers.size()));
			currentCall = (currentCall + 1) % maxTurn;
			if (goalAchiever.isPresent()) {
				goalAchievers.add(createGoalAchiever(goalAchiever.get(), xpGetters.get(index)));
				if (currentCall == 0) {
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
				goalAchievers.addAll(taskGoals);
				goalAchievers
						.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, goalFactory, bankDAO));
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
