package strategy.achiever.factory;

import java.util.List;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.GrandExchangeDAO;
import hydra.dao.ItemDAO;
import hydra.dao.MapDAO;
import hydra.dao.TaskDAO;
import hydra.dao.response.FightResponse;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotCraftSkill;
import hydra.model.BotMonster;
import hydra.model.BotResourceSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.custom.DepositGoldInBankGoalAchiever;
import strategy.achiever.factory.custom.DepositResourceGoalAchiever;
import strategy.achiever.factory.custom.DepositTaskCoinGoalAchiever;
import strategy.achiever.factory.custom.DepositToolGoalAchiever;
import strategy.achiever.factory.custom.EquipmentManagerGoalAchiever;
import strategy.achiever.factory.custom.ExtendBankSlotGoalAchiever;
import strategy.achiever.factory.custom.FoodManagerGoalAchiever;
import strategy.achiever.factory.custom.PotionManagerGoalAchiever;
import strategy.achiever.factory.custom.UseGoldItemManagerGoalAchiever;
import strategy.achiever.factory.custom.UselessEquipmentManagerGoalAchiever;
import strategy.achiever.factory.custom.UselessResourceManagerGoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.EquipToolGoalAchiever;
import strategy.achiever.factory.goals.GatheringGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose;
import strategy.achiever.factory.goals.GoalAchieverList;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.GoalAchieverTwoStep;
import strategy.achiever.factory.goals.ItemCraftGoalAchiever;
import strategy.achiever.factory.goals.ItemGetBankGoalAchiever;
import strategy.achiever.factory.goals.ItemGetInventoryOrBankGoalAchiever;
import strategy.achiever.factory.goals.ItemMonsterGoalAchiever;
import strategy.achiever.factory.goals.ItemRecycleGoalAchiever;
import strategy.achiever.factory.goals.ItemTaskGoalAchiever;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.goals.MonsterTaskGoalAchiever;
import strategy.achiever.factory.goals.ResourceGoalAchiever;
import strategy.achiever.factory.goals.TradeGoalAchiever;
import strategy.achiever.factory.goals.UnequipFirstWeaponGoalAchiever;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.ItemService;
import strategy.achiever.factory.util.SlotMethod;
import strategy.achiever.factory.util.StopChecker;
import strategy.util.CharacterService;
import strategy.util.MonsterEquipementService;
import strategy.util.MoveService;
import strategy.util.fight.FightService;

public class GoalFactoryCreatorImpl implements GoalFactoryCreator {

	private final CharacterDAO characterDAO;
	private final BankDAO bankDAO;
	private final MoveService moveService;
	private final CharacterService characterService;
	private final ItemService itemService;
	private final GoalParameter parameter;
	private final ItemDAO itemDao;
	private final GrandExchangeDAO grandExchangeDAO;
	private final FightService fightService;
	private final TaskDAO taskDAO;
	private final MapDAO mapDAO;
	private final MonsterEquipementService monsterEquipementService;

	public GoalFactoryCreatorImpl(CharacterDAO characterDAO, BankDAO bankDAO, ItemDAO itemDao,
			GrandExchangeDAO grandExchangeDAO, TaskDAO taskDAO, MapDAO mapDAO, MoveService moveService,
			CharacterService characterService, ItemService itemService, FightService fightService,
			MonsterEquipementService monsterEquipementService, GoalParameter parameter) {
		this.characterDAO = characterDAO;
		this.bankDAO = bankDAO;
		this.itemDao = itemDao;
		this.grandExchangeDAO = grandExchangeDAO;
		this.taskDAO = taskDAO;
		this.mapDAO = mapDAO;
		this.moveService = moveService;
		this.characterService = characterService;
		this.itemService = itemService;
		this.fightService = fightService;
		this.monsterEquipementService = monsterEquipementService;
		this.parameter = parameter;
	}

	@Override
	public GoalAchiever createUseGoldItemManagerGoalAchiever(List<String> goldResourceItems) {
		return new UseGoldItemManagerGoalAchiever(characterDAO, characterService, goldResourceItems);
	}

	@Override
	public GoalAchiever createDepositGoldInBankGoalAchiever() {
		return new DepositGoldInBankGoalAchiever(characterDAO, bankDAO, moveService, characterService);
	}

	@Override
	public GoalAchiever createExtendBankSlotGoalAchiever() {
		return new ExtendBankSlotGoalAchiever(characterDAO, bankDAO, moveService, characterService);
	}

	@Override
	public GoalAchiever createDepositToolGoalAchiever() {
		return new DepositToolGoalAchiever(bankDAO, itemService, moveService, parameter, characterService);
	}

	@Override
	public GoalAchiever createDepositTaskCoinGoalAchiever() {
		return new DepositTaskCoinGoalAchiever(bankDAO, moveService, parameter, characterService);
	}

	@Override
	public GoalAchiever createDepositResourceGoalAchiever(List<String> resourceItemsCraftable) {
		return new DepositResourceGoalAchiever(bankDAO, moveService, resourceItemsCraftable, parameter,
				characterService);
	}

	@Override
	public GoalAchiever createEquipmentManagerGoalAchiever() {
		return new EquipmentManagerGoalAchiever(characterDAO, itemDao, bankDAO, grandExchangeDAO, moveService,
				parameter, characterService);
	}

	@Override
	public GoalAchiever createUselessEquipmentManagerGoalAchiever(List<BotMonster> monsters) {
		return new UselessEquipmentManagerGoalAchiever(characterDAO, itemDao, bankDAO, grandExchangeDAO, fightService,
				monsters, moveService, itemService, characterService);
	}

	@Override
	public GoalAchiever createUselessResourceManagerGoalAchiever(List<String> rareResourceItems) {
		return new UselessResourceManagerGoalAchiever(characterDAO, bankDAO, itemDao, characterService, moveService,
				rareResourceItems);
	}

	@Override
	public GoalAchiever createFoodManagerGoalAchiever() {
		return new FoodManagerGoalAchiever(itemDao, bankDAO, parameter, moveService, characterService);
	}

	@Override
	public GoalAchiever createPotionManagerGoalAchiever() {
		return new PotionManagerGoalAchiever(itemDao, bankDAO, parameter, moveService, characterService);
	}

	@Override
	public GoalAchiever createDepositNoReservedItemGoalAchiever() {
		return new DepositNoReservedItemGoalAchiever(bankDAO, moveService, characterService, parameter);
	}

	@Override
	public ItemGetBankGoalAchiever createItemGetBankGoalAchiever(String code) {
		return new ItemGetBankGoalAchiever(bankDAO, code, moveService, characterService);
	}

	@Override
	public ItemGetBankGoalAchiever createItemGetBankGoalAchieverForceNoRoot(String code) {
		return new ItemGetBankGoalAchiever(bankDAO, code, moveService, characterService) {
			@Override
			public void setRoot() {
				// On force le comportement comme s'il n'est pas root
				super.unsetRoot();
			}
		};
	}

	@Override
	public ArtifactGoalAchiever createGoalAchieverLoop(GoalAchiever subGoal, int quantity, boolean virtualRoot) {
		return new GoalAchieverLoop(subGoal, quantity, virtualRoot);
	}

	@Override
	public ArtifactGoalAchiever createTradeGoalAchiever(List<Coordinate> coordinates, String code, int quantity) {
		return new TradeGoalAchiever(moveService, taskDAO, coordinates, code, quantity);
	}

	@Override
	public GoalAchiever createEquipToolGoalAchiever(BotResourceSkill resourceSkill) {
		return new EquipToolGoalAchiever(characterDAO, bankDAO, moveService, characterService, itemService,
				resourceSkill);
	}

	@Override
	public GoalAchiever createItemRecycleGoalAchiever(String code, BotCraftSkill botCraftSkill, int minPreserve) {
		return new ItemRecycleGoalAchiever(code, characterDAO, moveService, characterService, botCraftSkill,
				minPreserve);
	}

	@Override
	public ArtifactGoalAchiever createGoalAchieverTwoStep(GoalAchiever optionalGoal, GoalAchiever goal,
			boolean virtualRoot, boolean checkBeforeExecuteOptional) {
		return new GoalAchieverTwoStep(characterDAO, optionalGoal, goal, virtualRoot, checkBeforeExecuteOptional);
	}

	@Override
	public ResourceGoalAchiever createGatheringGoalAchiever(String resourceCode, int rate, List<Coordinate> coordinates,
			int level, BotResourceSkill skill, String boxCode) {
		return new GatheringGoalAchiever(characterDAO, characterService, mapDAO, resourceCode, rate, coordinates, level,
				skill, boxCode, moveService);
	}

	@Override
	public ResourceGoalAchiever createItemMonsterGoalAchiever(String resourceCode, int rate,
			List<Coordinate> coordinates, BotMonster monster) {
		return new ItemMonsterGoalAchiever(characterDAO, mapDAO, resourceCode, rate, coordinates, monster,
				monsterEquipementService, fightService, moveService, characterService, parameter);
	}

	@Override
	public MonsterGoalAchiever createMonsterGoalAchiever(List<Coordinate> coordinates, BotMonster monster,
			StopChecker<FightResponse> stopCondition) {
		return new MonsterGoalAchiever(characterDAO, mapDAO, coordinates, monster, monsterEquipementService,
				stopCondition, fightService, moveService, parameter);
	}

	@Override
	public GoalAchiever createMonsterTaskGoalAchiever(List<Coordinate> coordinates) {
		return new MonsterTaskGoalAchiever(characterDAO, taskDAO, bankDAO, coordinates, moveService, characterService,
				parameter);
	}

	@Override
	public GoalAchiever createItemTaskGoalAchiever(List<Coordinate> coordinates) {
		return new ItemTaskGoalAchiever(characterDAO, taskDAO, bankDAO, coordinates, characterService, moveService,
				parameter);
	}

	@Override
	public ArtifactGoalAchiever createItemGetInventoryOrBankGoalAchiever(String code,
			ItemGetBankGoalAchiever itemGetBankGoalAchiever, int quantity) {
		return new ItemGetInventoryOrBankGoalAchiever(characterService, code, itemGetBankGoalAchiever, quantity);
	}

	@Override
	public GoalAchieverChoose createGoalAchieverChoose(ChooseBehaviorSelector chooseBehaviorSelector) {
		return new GoalAchieverChoose(characterService, chooseBehaviorSelector);
	}

	@Override
	public ResourceGoalAchiever createItemCraftGoalAchiever(String code, int level, BotCraftSkill skill,
			ArtifactGoalAchiever goalAchiever) {
		return new ItemCraftGoalAchiever(characterDAO, characterService, code, moveService, level, skill, goalAchiever);
	}

	@Override
	public ResourceGoalAchiever createUnequipFirstWeaponGoalAchiever(SlotMethod method,
			BotCharacterInventorySlot slot) {
		return new UnequipFirstWeaponGoalAchiever(characterDAO, method, slot);
	}
	
	@Override
	public GoalAchieverList createGoalAchieverList() {
		return new GoalAchieverList();
	}
}
