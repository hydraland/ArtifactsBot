package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotResourceSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalAchieverConditional;
import strategy.achiever.GoalAchieverConditional.Condition;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo.INFO_TYPE;
import strategy.util.Bornes;
import strategy.util.CharacterService;
import strategy.util.OneExecutionCondition;
import strategy.util.StrategySkillUtils;

public final class BalanceRateStrategy implements Strategy {

	private final CharacterDAO characterDAO;
	private final List<GoalAchiever> inventoryGoals;
	private final List<ArtifactGoalAchiever> itemGoals;
	private final List<MonsterGoalAchiever> monsterGoals;
	private final List<GoalAchiever> taskGoals;

	private static final int[][] SKILL_LEVEL_LIST = new int[][] { Strategy.FISHING_LEVELS, Strategy.COOKING_LEVELS,
			Strategy.WOODCUTTING_LEVELS, Strategy.GEARCRAFTING_LEVELS, Strategy.MINING_LEVELS,
			Strategy.WEAPONCRAFTING_LEVELS, Strategy.JEWELRYCRAFTING_LEVELS, Strategy.ALCHEMY_LEVELS };
	private List<ArtifactGoalAchiever> dropItemGoal;
	private final CharacterService characterService;
	private final GoalFactory goalFactory;
	private GoalAchiever eventGoal;
	private final BankDAO bankDAO;
	private final List<MonsterGoalAchiever> monsterGoalsForEvent;

	public BalanceRateStrategy(CharacterDAO characterDAO, GoalFactory goalFactory, CharacterService characterService,
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
		dropItemGoal = goalFactory.getDropItemGoal();
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
		int charLevel = character.getLevel();
		if (skillLevels[index] < GameConstants.MAX_SKILL_LEVEL || charLevel < GameConstants.MAX_LEVEL) {

			if (skillLevels[index] <= charLevel) {
				// recherche tous les buts pour augmenter le skillMin
				Bornes bornes = StrategySkillUtils.getBorneLevel(skillLevels[index], SKILL_LEVEL_LIST[index]);

				List<Predicate<ArtifactGoalAchiever>> filterPredicate = new ArrayList<>(SKILL_LEVEL_LIST.length);
				filterPredicate.addAll(createFiltersPredicate(goalFactory, bornes));

				List<ArtifactGoalAchiever> searchGoalAchievers = allGoals.stream().filter(filterPredicate.get(index))
						.sorted((c1, c2) -> Double.compare(c1.getRate(), c2.getRate())).toList().reversed();
				ArrayList<GoalAchiever> goalAchievers = new ArrayList<>();
				Optional<ArtifactGoalAchiever> goalAchiever = searchGoalAchievers.stream()
						.filter(ga -> ga.isRealisableAfterSetRoot(character)).findFirst();
				if (goalAchiever.isPresent()) {
					goalAchievers.addAll(
							searchGoalAchievers.stream().filter(ga -> ga.isRealisableAfterSetRoot(character)).toList());
					List<Condition> conditions = new ArrayList<>(SKILL_LEVEL_LIST.length);
					conditions.add(() -> characterDAO.getCharacter().getFishingLevel() > skillLevels[0]);
					conditions.add(() -> characterDAO.getCharacter().getCookingLevel() > skillLevels[1]);
					conditions.add(() -> characterDAO.getCharacter().getWoodcuttingLevel() > skillLevels[2]);
					conditions.add(() -> characterDAO.getCharacter().getGearcraftingLevel() > skillLevels[3]);
					conditions.add(() -> characterDAO.getCharacter().getMiningLevel() > skillLevels[4]);
					conditions.add(() -> characterDAO.getCharacter().getWeaponcraftingLevel() > skillLevels[5]);
					conditions.add(() -> characterDAO.getCharacter().getJewelrycraftingLevel() > skillLevels[6]);
					conditions.add(() -> characterDAO.getCharacter().getAlchemyLevel() > skillLevels[7]);
					goalAchievers.add(createGoalAchiever(goalAchiever.get(), conditions.get(index)));
				} else {
					bornes = new Bornes(bornes.oldMin(), bornes.oldMin(), bornes.min());
					filterPredicate.clear();
					filterPredicate.addAll(createFiltersPredicate(goalFactory, bornes));
					goalAchievers.addAll(allGoals.stream().filter(filterPredicate.get(index))
							.map(ga -> createGoalAchiever(ga, new OneExecutionCondition())).toList());
				}
				goalAchievers
						.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, goalFactory, bankDAO));
				goalAchievers.addAll(taskGoals);
				return goalAchievers;
			} else {
				ArrayList<GoalAchiever> goalAchievers = new ArrayList<>();
				// recherche monstres
				List<MonsterGoalAchiever> monstersGoal = monsterGoals.stream()
						.filter(mga -> mga.getMonsterLevel() <= charLevel)
						.sorted((c1, c2) -> Integer.compare(c1.getMonsterLevel(), c2.getMonsterLevel())).toList()
						.reversed();
				for (MonsterGoalAchiever aGoal : monstersGoal) {
					goalAchievers.add(new GoalAchieverConditional(aGoal,
							() -> characterDAO.getCharacter().getLevel() > charLevel, true));
				}
				goalAchievers
						.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, goalFactory, bankDAO));
				goalAchievers.addAll(taskGoals);
				return goalAchievers;
			}
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

	private GoalAchiever createGoalAchiever(ArtifactGoalAchiever goalAchiever, Condition condition) {
		BotCraftSkill botCraftSkill = goalFactory.getInfos(goalAchiever).getBotCraftSkill();
		if (goalFactory.getInfos(goalAchiever).isCraft() && (botCraftSkill.equals(BotCraftSkill.WEAPONCRAFTING)
				|| botCraftSkill.equals(BotCraftSkill.GEARCRAFTING)
				|| botCraftSkill.equals(BotCraftSkill.JEWELRYCRAFTING))) {
			GoalAchiever goalAchieverWithRecycle = goalFactory.addItemRecycleGoalAchiever(goalAchiever,
					Strategy.calculMinItemPreserve(goalFactory, goalAchiever));
			return new GoalAchieverConditional(goalAchieverWithRecycle, condition, true);
		} else {
			return new GoalAchieverConditional(goalAchiever, condition, true);
		}
	}

	static List<Predicate<ArtifactGoalAchiever>> createFiltersPredicate(GoalFactory factory, Bornes bornes) {
		ArrayList<Predicate<ArtifactGoalAchiever>> filterPredicate = new ArrayList<>();
		filterPredicate
				.add(StrategySkillUtils.createFilterResourcePredicate(factory, bornes, BotResourceSkill.FISHING));
		filterPredicate.add(StrategySkillUtils.createFilterCraftPredicate(factory, BotCraftSkill.COOKING, bornes));
		filterPredicate.add(StrategySkillUtils.createFilterResourceAndCraftPredicate(factory, bornes,
				BotResourceSkill.WOODCUTTING, BotCraftSkill.WOODCUTTING));
		filterPredicate.add(StrategySkillUtils.createFilterCraftPredicate(factory, BotCraftSkill.GEARCRAFTING, bornes));
		filterPredicate.add(StrategySkillUtils.createFilterResourceAndCraftPredicate(factory, bornes,
				BotResourceSkill.MINING, BotCraftSkill.MINING));
		filterPredicate
				.add(StrategySkillUtils.createFilterCraftPredicate(factory, BotCraftSkill.WEAPONCRAFTING, bornes));
		filterPredicate
				.add(StrategySkillUtils.createFilterCraftPredicate(factory, BotCraftSkill.JEWELRYCRAFTING, bornes));
		filterPredicate.add(StrategySkillUtils.createFilterResourceAndCraftPredicate(factory, bornes,
				BotResourceSkill.ALCHEMY, BotCraftSkill.ALCHEMY));
		return filterPredicate;
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
