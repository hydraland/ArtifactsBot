package strategy.achiever.factory.custom;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotItemType;
import strategy.achiever.GoalParameter;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public final class FoodManagerGoalAchiever extends AbstractCustomGoalAchiever {

	private final GoalParameter parameter;
	private final CustomCondition condition;
	private final MoveService moveService;
	private final BankDAO bankDAO;

	public FoodManagerGoalAchiever(ItemDAO itemDAO, BankDAO bankDAO, GoalParameter parameter, MoveService moveService,
			CharacterService characterService) {
		super(characterService);
		this.bankDAO = bankDAO;
		this.parameter = parameter;
		this.moveService = moveService;
		condition = item -> BotItemType.UTILITY.equals(itemDAO.getItem(item.getCode()).getType());
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return characterService.getInventoryFreeSlotNumber() < parameter.getMinFreeSlot() && isDeposit(condition);
	}

	@Override
	public boolean execute() {
		return deposit(bankDAO, moveService, condition);
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
