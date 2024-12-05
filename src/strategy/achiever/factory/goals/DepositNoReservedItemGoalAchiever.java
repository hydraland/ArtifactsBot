package strategy.achiever.factory.goals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.model.BotCharacter;
import hydra.model.BotInventoryItem;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public final class DepositNoReservedItemGoalAchiever implements GoalAchiever {

	private boolean finish;
	private final BankDAO bankDao;
	private final CharacterService characterService;
	private final MoveService moveService;
	private final GoalParameter goalParameter;

	public DepositNoReservedItemGoalAchiever(BankDAO bankDao, MoveService moveService,
			CharacterService characterService, GoalParameter goalParameter) {
		this.bankDao = bankDao;
		this.moveService = moveService;
		this.characterService = characterService;
		this.goalParameter = goalParameter;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return characterService.getInventoryFreeSlotNumber() < goalParameter.getMinFreeSlot()
				|| characterService.getFreeInventorySpace() < goalParameter.getMinFreeInventorySpace();
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			List<BotInventoryItem> itemToDeposit = new ArrayList<>();
			for (BotInventoryItem botInventoryItem : characterService.getInventoryIgnoreEmpty()) {
				if (!reservedItems.containsKey(botInventoryItem.getCode())) {
					itemToDeposit.add(botInventoryItem);
				}
			}
			if (!itemToDeposit.isEmpty()) {
				if (moveService.moveToBank()) {
					for (BotInventoryItem botInventoryItem : itemToDeposit) {
						if (!bankDao.deposit(botInventoryItem)) {
							return false;
						}
					}
					return true;
				}
				return false;
			}
			return true;
		} finally {
			this.finish = true;
		}
	}

	@Override
	public boolean isFinish() {
		return finish;
	}

	@Override
	public void clear() {
		this.finish = false;
	}

	@Override
	public void setRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	@Override
	public void unsetRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
