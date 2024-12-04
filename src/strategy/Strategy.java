package strategy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import hydra.model.BotItemType;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalAchieverConditional;
import strategy.achiever.factory.ArtifactGoalAchiever;
import strategy.achiever.factory.GoalAchieverInfo;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.MonsterGoalAchiever;
import strategy.util.CharacterService;

public interface Strategy {

	static final int[] FISHING_LEVELS = new int[] { 1, 10, 20, 30, 40 };
	static final int[] COOKING_LEVELS = new int[] { 1, 5, 10, 15, 20, 30, 40 };
	static final int[] WOODCUTTING_LEVELS = new int[] { 1, 10, 20, 30, 35, 40 };
	static final int[] GEARCRAFTING_LEVELS = new int[] { 1, 5, 10, 15, 20, 25, 30, 35, 40 };
	static final int[] MINING_LEVELS = new int[] { 1, 10, 20, 30, 35, 40 };
	static final int[] WEAPONCRAFTING_LEVELS = new int[] { 1, 5, 10, 15, 20, 25, 30, 35, 40 };
	static final int[] JEWELRYCRAFTING_LEVELS = new int[] { 1, 5, 10, 15, 20, 25, 30, 35, 40 };
	static final int[] ALCHEMY_LEVELS = new int[] { 1, 5, 10, 20, 30, 35, 40 };

	public Iterable<GoalAchiever> getGoalAchievers();

	public Iterable<GoalAchiever> getManagedInventoryCustomGoal();

	public static List<ArtifactGoalAchiever> filterDropItemGoals(List<ArtifactGoalAchiever> dropItemGoal,
			CharacterService characterService, GoalFactory goalFactory, BankDAO bankDAO) {
		return dropItemGoal.stream()
				.filter(aga -> !characterService.isPossess(goalFactory.getInfos(aga).getItemCode(), bankDAO)).toList();
	}

	public static List<ArtifactGoalAchiever> filterTaskGoals(List<ArtifactGoalAchiever> itemsGoal,
			CharacterService characterService, GoalFactory goalFactory, BankDAO bankDAO) {
		Predicate<? super ArtifactGoalAchiever> predicate = goal -> {
			GoalAchieverInfo infos = goalFactory.getInfos(goal);
			if (infos.isNeedTaskMasterResource()) {
				if (BotItemType.RING.equals(infos.getItemType())) {
					return !characterService.isPossess(infos.getItemCode(), 2, bankDAO);
				} else {
					return !characterService.isPossess(infos.getItemCode(), bankDAO);
				}
			}
			return true;
		};
		return itemsGoal.stream().filter(predicate).toList();
	}

	public static int calculMinItemPreserve(GoalFactory goalFactory, ArtifactGoalAchiever goalAchiever) {
		return BotItemType.RING.equals(goalFactory.getInfos(goalAchiever).getItemType()) ? 2 : 1;
	}

	public boolean isAcceptEvent(String type, String code);

	public void initializeGoal(String type, String code);

	public GoalAchiever getEventGoalAchiever();

	static final GoalAchiever NOTHING_GOAL = new GoalAchiever() {

		@Override
		public void unsetRoot() {
			// Le fait d'être noeud racine ou pas ne change pas l'implémentation
		}

		@Override
		public void setRoot() {
			// Le fait d'être noeud racine ou pas ne change pas l'implémentation
		}

		@Override
		public boolean isRealisable(BotCharacter character) {
			return false;
		}

		@Override
		public boolean isFinish() {
			return true;
		}

		@Override
		public boolean execute(Map<String, Integer> reservedItems) {
			return false;
		}

		@Override
		public void clear() {
			// Ne fait rien
		}

		@Override
		public String toString() {
			return "NOTHING_GOAL";
		}
	};

	public static boolean isAcceptEvent(GoalFactory goalFactory, CharacterDAO characterDAO, String type, String code,
			List<MonsterGoalAchiever> monsterGoals, List<ArtifactGoalAchiever> itemGoals) {
		if ("monster".equals(type)) {
			Optional<MonsterGoalAchiever> goalAchiever = monsterGoals.stream()
					.filter(mga -> code.equals(mga.getMonsterCode())).findFirst();
			return goalAchiever.isPresent() && goalAchiever.get().isRealisableAfterSetRoot(characterDAO.getCharacter());
		} else if ("resource".equals(type)) {
			Optional<ArtifactGoalAchiever> goalAchiever = itemGoals.stream()
					.filter(aga -> code.equals(goalFactory.getInfos(aga).getItemCode())).findFirst();
			return goalAchiever.isPresent() && goalAchiever.get().isRealisableAfterSetRoot(characterDAO.getCharacter());
		}
		return false;
	}

	public static GoalAchiever initializeGoal(GoalFactory goalFactory, String type,
			String code, List<MonsterGoalAchiever> monsterGoals, List<ArtifactGoalAchiever> itemGoals) {
		if ("monster".equals(type)) {
			MonsterGoalAchiever goalAchiever = monsterGoals.stream().filter(mga -> code.equals(mga.getMonsterCode()))
					.findFirst().get();
			return new GoalAchieverConditional(goalAchiever, () -> true, true);
		} else if ("resource".equals(type)) {
			ArtifactGoalAchiever goalAchiever = itemGoals.stream()
					.filter(aga -> code.equals(goalFactory.getInfos(aga).getItemCode())).findFirst().get();
			return new GoalAchieverConditional(goalFactory.addDepositNoReservedItemGoalAchiever(goalAchiever), () -> true, true);
		}
		return NOTHING_GOAL;
	}
}
