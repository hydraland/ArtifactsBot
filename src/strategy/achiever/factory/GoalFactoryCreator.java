package strategy.achiever.factory;

import java.util.List;

import hydra.model.BotMonster;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.custom.DepositGoldInBankGoalAchiever;
import strategy.achiever.factory.custom.DepositResourceGoalAchiever;
import strategy.achiever.factory.custom.DepositTaskCoinGoalAchiever;
import strategy.achiever.factory.custom.DepositToolGoalAchiever;
import strategy.achiever.factory.custom.EquipmentManagerGoalAchiever;
import strategy.achiever.factory.custom.ExtendBankSlotGoalAchiever;
import strategy.achiever.factory.custom.FoodManagerGoalAchiever;
import strategy.achiever.factory.custom.PotionManagerGoalAchiever;
import strategy.achiever.factory.custom.UselessEquipmentManagerGoalAchiever;
import strategy.achiever.factory.custom.UselessResourceManagerGoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.ItemGetBankGoalAchiever;

public interface GoalFactoryCreator {

	DepositGoldInBankGoalAchiever createDepositGoldInBankGoalAchiever();

	ExtendBankSlotGoalAchiever createExtendBankSlotGoalAchiever();

	DepositToolGoalAchiever createDepositToolGoalAchiever();

	DepositTaskCoinGoalAchiever createDepositTaskCoinGoalAchiever();

	DepositResourceGoalAchiever createDepositResourceGoalAchiever(List<String> resourceItemsCraftable);

	EquipmentManagerGoalAchiever createEquipmentManagerGoalAchiever();

	UselessEquipmentManagerGoalAchiever createUselessEquipmentManagerGoalAchiever(List<BotMonster> monsters);

	UselessResourceManagerGoalAchiever createUselessResourceManagerGoalAchiever(List<String> rareResourceItems);

	FoodManagerGoalAchiever createFoodManagerGoalAchiever();

	PotionManagerGoalAchiever createPotionManagerGoalAchiever();

	DepositNoReservedItemGoalAchiever createDepositNoReservedItemGoalAchiever();

	ItemGetBankGoalAchiever createItemGetBankGoalAchiever(String code);

	ItemGetBankGoalAchiever createItemGetBankGoalAchieverForceNoRoot(String code);

	GoalAchieverLoop createGoalAchieverLoop(GoalAchiever subGoal, int quantity);

}