package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.ArtifactGoalAchiever;
import strategy.achiever.factory.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.GoalAchieverInfo.INFO_TYPE;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.MonsterGoalAchiever;
import strategy.util.Bornes;
import strategy.util.CharacterService;
import strategy.util.StrategySkillUtils;

public final class CraftLevelStrategy implements Strategy {

	private final CharacterDAO characterDAO;
	private final List<GoalAchiever> inventoryGoals;
	private final List<ArtifactGoalAchiever> itemGoals;
	private final List<MonsterGoalAchiever> monsterGoals;
	private final List<GoalAchiever> taskGoals;
	private final List<ArtifactGoalAchiever> dropItemGoal;
	private static final int[][] SKILL_LEVEL_LIST = new int[][] { Strategy.GEARCRAFTING_LEVELS,
			Strategy.WEAPONCRAFTING_LEVELS, Strategy.JEWELRYCRAFTING_LEVELS };
	private final CharacterService characterService;
	private final GoalFactory goalFactory;
	private GoalAchiever eventGoal;
	private final BankDAO bankDAO;
	private final List<MonsterGoalAchiever> monsterGoalsForEvent;

	@Deprecated
	public CraftLevelStrategy(CharacterDAO characterDAO, GoalFactory goalFactory, CharacterService characterService,
			BankDAO bankDAO) {
		this.characterDAO = characterDAO;
		this.goalFactory = goalFactory;
		this.characterService = characterService;
		this.bankDAO = bankDAO;
		itemGoals = goalFactory.createItemsGoals(() -> ChooseBehaviorSelector.CRAFTING);
		inventoryGoals = goalFactory.createManagedInventoryCustomGoal();
		monsterGoals = goalFactory.createMonstersGoals(resp -> resp.fight().getXp() == 0);
		monsterGoalsForEvent = goalFactory.createMonstersGoals(resp -> false);
		taskGoals = goalFactory.createTaskGoals(resp -> !resp.fight().isWin());
		dropItemGoal = goalFactory.getDropItemGoal();
	}

	@Override
	public Iterable<GoalAchiever> getGoalAchievers() {
		BotCharacter character = this.characterDAO.getCharacter();
		int[] skillLevels = new int[] { character.getGearcraftingLevel(), character.getWeaponcraftingLevel(),
				character.getJewelrycraftingLevel() };
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

				List<ArtifactGoalAchiever> tmpGoalAchievers = allGoals.stream().filter(filterPredicate.get(index))
						.toList();
				List<GoalAchiever> goalAchievers = new ArrayList<>();
				goalAchievers.addAll(tmpGoalAchievers.stream().filter(goal -> goal.isRealisableAfterSetRoot(character))
						.map(this::createGoalAchiever).toList());

				if (goalAchievers.isEmpty()) {
					bornes = new Bornes(bornes.oldMin(), bornes.oldMin(), bornes.max());
					filterPredicate.clear();
					filterPredicate.addAll(createFiltersPredicate(goalFactory, bornes));
					goalAchievers.addAll(allGoals.stream().filter(filterPredicate.get(index)).toList());
				}
				goalAchievers
						.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, goalFactory, bankDAO));
				goalAchievers.addAll(taskGoals);
				return goalAchievers;
			} else {
				ArrayList<GoalAchiever> goalAchievers = new ArrayList<>();
				// recherche monstres
				List<MonsterGoalAchiever> monstersGoal = monsterGoals.stream()
						.filter(mga -> mga.getMonsterLevel() <= charLevel).toList();
				goalAchievers.addAll(monstersGoal);
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
		goalAchievers.addAll(taskGoals);
		goalAchievers.addAll(Strategy.filterDropItemGoals(dropItemGoal, characterService, goalFactory, bankDAO));
		return goalAchievers;
	}

	private GoalAchiever createGoalAchiever(ArtifactGoalAchiever goalAchiever) {
		BotCraftSkill botCraftSkill = goalFactory.getInfos(goalAchiever).getBotCraftSkill();
		if (goalFactory.getInfos(goalAchiever).isCraft() && (botCraftSkill.equals(BotCraftSkill.WEAPONCRAFTING)
				|| botCraftSkill.equals(BotCraftSkill.GEARCRAFTING)
				|| botCraftSkill.equals(BotCraftSkill.JEWELRYCRAFTING))) {
			return goalFactory.addItemRecycleGoalAchiever(goalAchiever,
					Strategy.calculMinItemPreserve(goalFactory, goalAchiever));
		} else {
			return goalAchiever;
		}
	}

	private List<Predicate<ArtifactGoalAchiever>> createFiltersPredicate(GoalFactory factory, Bornes bornes) {
		List<Predicate<ArtifactGoalAchiever>> filterPredicate = new ArrayList<>();
		filterPredicate.add(StrategySkillUtils.createFilterCraftPredicate(factory, BotCraftSkill.GEARCRAFTING, bornes));
		filterPredicate
				.add(StrategySkillUtils.createFilterCraftPredicate(factory, BotCraftSkill.WEAPONCRAFTING, bornes));
		filterPredicate
				.add(StrategySkillUtils.createFilterCraftPredicate(factory, BotCraftSkill.JEWELRYCRAFTING, bornes));
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