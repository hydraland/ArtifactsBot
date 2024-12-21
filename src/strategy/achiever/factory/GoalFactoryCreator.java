package strategy.achiever.factory;

import java.util.List;

import hydra.dao.CharacterDAO;
import hydra.dao.response.FightResponse;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotCraftSkill;
import hydra.model.BotMonster;
import hydra.model.BotResourceSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalAchieverConditional.Condition;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GenericGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.GoalAchieverList;
import strategy.achiever.factory.goals.ItemGetBankGoalAchiever;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.goals.MonsterItemDropGoalAchiever;
import strategy.achiever.factory.goals.ResourceGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.SlotMethod;
import strategy.achiever.factory.util.StopChecker;

public interface GoalFactoryCreator {

	GoalAchiever createDepositGoldInBankGoalAchiever();

	GoalAchiever createExtendBankSlotGoalAchiever();

	GoalAchiever createDepositToolGoalAchiever();

	GoalAchiever createDepositTaskCoinGoalAchiever();

	GoalAchiever createDepositResourceGoalAchiever(List<String> resourceItemsCraftable);

	GoalAchiever createEquipmentManagerGoalAchiever();

	GoalAchiever createUselessEquipmentManagerGoalAchiever(List<BotMonster> monsters);

	GoalAchiever createUselessResourceManagerGoalAchiever(List<String> rareResourceItems);

	GoalAchiever createFoodManagerGoalAchiever();

	GoalAchiever createPotionManagerGoalAchiever();

	GoalAchiever createDepositNoReservedItemGoalAchiever();

	ItemGetBankGoalAchiever createItemGetBankGoalAchiever(String code);

	ItemGetBankGoalAchiever createItemGetBankGoalAchieverForceNoRoot(String code);

	ArtifactGoalAchiever createGoalAchieverLoop(GoalAchiever subGoal, int quantity, boolean virtualRoot);

	ArtifactGoalAchiever createTradeGoalAchiever(List<Coordinate> coordinates, String code, int quantity);

	GoalAchiever createEquipToolGoalAchiever(BotResourceSkill resourceSkill);

	GoalAchiever createItemRecycleGoalAchiever(String code, BotCraftSkill botCraftSkill, int minPreserve);

	ArtifactGoalAchiever createGoalAchieverTwoStep(GoalAchiever optionalGoal, GoalAchiever goal, boolean virtualRoot,
			boolean checkBeforeExecuteOptional);

	ResourceGoalAchiever createGatheringGoalAchiever(String resourceCode, int rate, List<Coordinate> coordinates,
			int level, BotResourceSkill skill, String boxCode, boolean event);

	ResourceGoalAchiever createItemMonsterGoalAchiever(
			String resourceCode, int rate, List<Coordinate> coordinates,
			BotMonster monster, boolean event);

	MonsterGoalAchiever createMonsterGoalAchiever(List<Coordinate> coordinates, BotMonster monster,
			StopChecker<FightResponse> stopCondition, boolean event);

	GoalAchiever createMonsterTaskGoalAchiever(List<Coordinate> coordinates);

	GoalAchiever createItemTaskGoalAchiever(List<Coordinate> coordinates);

	ArtifactGoalAchiever createItemGetInventoryOrBankGoalAchiever(String code,
			ItemGetBankGoalAchiever itemGetBankGoalAchiever, int quantity);

	GoalAchieverChoose createGoalAchieverChoose(ChooseBehaviorSelector chooseBehaviorSelector);

	ResourceGoalAchiever createItemCraftGoalAchiever(String code, int level, BotCraftSkill skill,
			ArtifactGoalAchiever goalAchiever);

	GoalAchiever createUseGoldItemManagerGoalAchiever(List<String> goldResourceItems);

	ResourceGoalAchiever createUnequipFirstWeaponGoalAchiever(SlotMethod method, BotCharacterInventorySlot slot);

	GoalAchieverList createGoalAchieverList();

	GenericGoalAchiever createGenericGoalAchiever();

	GoalAchiever createGoalAchieverConditional(GoalAchiever subGoal, Condition condition, boolean virtualRoot);

	MonsterItemDropGoalAchiever createMonsterItemDropGoalAchiever(GoalAchieverInfo<ArtifactGoalAchiever> goalInfo, CharacterDAO characterDao,
			GoalParameter parameter);
}