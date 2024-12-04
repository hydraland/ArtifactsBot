package strategy.achiever.factory.custom;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.model.BotCharacter;
import strategy.achiever.GoalParameter;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class DepositResourceGoalAchiever extends AbstractCustomGoalAchiever {

	private final BankDAO bankDAO;
	private final MoveService moveService;
	private final CustomCondition condition;
	private final GoalParameter parameter;

	public DepositResourceGoalAchiever(BankDAO bankDAO, MoveService moveService,
			List<String> resourceItems, GoalParameter parameter, CharacterService characterService) {
		super(characterService);
		this.bankDAO = bankDAO;
		this.moveService = moveService;
		this.parameter = parameter;
		condition = item -> resourceItems.contains(item.getCode());
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return (characterService.getInventoryFreeSlotNumber() < parameter.getMinFreeSlot()
				|| characterService.getFreeInventorySpace() < parameter.getMinFreeInventorySpace())
				&& isDeposit(condition);
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