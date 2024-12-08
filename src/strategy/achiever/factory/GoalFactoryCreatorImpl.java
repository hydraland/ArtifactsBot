package strategy.achiever.factory;

import java.util.List;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.GrandExchangeDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotMonster;
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
import strategy.achiever.factory.custom.UselessEquipmentManagerGoalAchiever;
import strategy.achiever.factory.custom.UselessResourceManagerGoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.ItemGetBankGoalAchiever;
import strategy.achiever.factory.util.ItemService;
import strategy.util.CharacterService;
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

	public GoalFactoryCreatorImpl(CharacterDAO characterDAO, BankDAO bankDAO, ItemDAO itemDao,
			GrandExchangeDAO grandExchangeDAO, MoveService moveService, CharacterService characterService,
			ItemService itemService, FightService fightService, GoalParameter parameter) {
		this.characterDAO = characterDAO;
		this.bankDAO = bankDAO;
		this.itemDao = itemDao;
		this.grandExchangeDAO = grandExchangeDAO;
		this.moveService = moveService;
		this.characterService = characterService;
		this.itemService = itemService;
		this.fightService = fightService;
		this.parameter = parameter;
	}

	@Override
	public DepositGoldInBankGoalAchiever createDepositGoldInBankGoalAchiever() {
		return new DepositGoldInBankGoalAchiever(characterDAO, bankDAO, moveService, characterService);
	}

	@Override
	public ExtendBankSlotGoalAchiever createExtendBankSlotGoalAchiever() {
		return new ExtendBankSlotGoalAchiever(characterDAO, bankDAO, moveService, characterService);
	}

	@Override
	public DepositToolGoalAchiever createDepositToolGoalAchiever() {
		return new DepositToolGoalAchiever(bankDAO, itemService, moveService, parameter, characterService);
	}

	@Override
	public DepositTaskCoinGoalAchiever createDepositTaskCoinGoalAchiever() {
		return new DepositTaskCoinGoalAchiever(bankDAO, moveService, parameter, characterService);
	}

	@Override
	public DepositResourceGoalAchiever createDepositResourceGoalAchiever(List<String> resourceItemsCraftable) {
		return new DepositResourceGoalAchiever(bankDAO, moveService, resourceItemsCraftable, parameter,
				characterService);
	}

	@Override
	public EquipmentManagerGoalAchiever createEquipmentManagerGoalAchiever() {
		return new EquipmentManagerGoalAchiever(characterDAO, itemDao, bankDAO, grandExchangeDAO, moveService,
				parameter, characterService);
	}

	@Override
	public UselessEquipmentManagerGoalAchiever createUselessEquipmentManagerGoalAchiever(List<BotMonster> monsters) {
		return new UselessEquipmentManagerGoalAchiever(characterDAO, itemDao, bankDAO, grandExchangeDAO, fightService,
				monsters, moveService, itemService, characterService);
	}

	@Override
	public UselessResourceManagerGoalAchiever createUselessResourceManagerGoalAchiever(List<String> rareResourceItems) {
		return new UselessResourceManagerGoalAchiever(characterDAO, bankDAO, itemDao, characterService, moveService,
				rareResourceItems);
	}

	@Override
	public FoodManagerGoalAchiever createFoodManagerGoalAchiever() {
		return new FoodManagerGoalAchiever(itemDao, bankDAO, parameter, moveService, characterService);
	}

	@Override
	public PotionManagerGoalAchiever createPotionManagerGoalAchiever() {
		return new PotionManagerGoalAchiever(itemDao, bankDAO, parameter, moveService, characterService);
	}

	@Override
	public DepositNoReservedItemGoalAchiever createDepositNoReservedItemGoalAchiever() {
		return new DepositNoReservedItemGoalAchiever(bankDAO, moveService, characterService, parameter);
	}
	
	@Override
	public ItemGetBankGoalAchiever createItemGetBankGoalAchiever(String code) {
		return new ItemGetBankGoalAchiever(bankDAO, code, moveService,
				characterService);
	}
	
	@Override
	public ItemGetBankGoalAchiever createItemGetBankGoalAchieverForceNoRoot(String code) {
		return new ItemGetBankGoalAchiever(bankDAO, code, moveService,
				characterService) {
			@Override
			public void setRoot() {
				// On force le comportement comme s'il n'est pas root
				super.unsetRoot();
			}
		};
	}
	
	@Override
	public GoalAchieverLoop createGoalAchieverLoop(GoalAchiever subGoal, int quantity) {
		return new GoalAchieverLoop(subGoal, quantity);
	}
}
