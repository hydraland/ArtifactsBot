package strategy.achiever.factory.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hydra.dao.BankDAO;
import hydra.model.BotInventoryItem;
import strategy.achiever.GoalAchiever;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public abstract class AbstractCustomGoalAchiever implements GoalAchiever {

	private boolean finish;
	protected final CharacterService characterService;

	protected AbstractCustomGoalAchiever(CharacterService characterService) {
		this.characterService = characterService;
		finish = false;
	}

	@Override
	public final boolean execute(Map<String, Integer> inventory) {
		try {
			return execute();
		} finally {
			finish = true;
		}
	}

	protected abstract boolean execute();

	@Override
	public final boolean isFinish() {
		return finish;
	}

	@Override
	public final void clear() {
		finish = false;
	}

	@Override
	public final void setRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	@Override
	public final void unsetRoot() {
		// Le fait d'être noeud racine ou pas ne change pas l'implémentation
	}

	protected final boolean deposit(BankDAO bankDAO, MoveService moveService,
			CustomCondition customCondition) {
		List<BotInventoryItem> inventoryItems = new ArrayList<>();
		for (BotInventoryItem item : characterService.getInventoryIgnoreEmpty()) {
			if (customCondition.accept(item)) {
				inventoryItems.add(item);
			}
		}

		if (!inventoryItems.isEmpty()) {
			if (moveService.moveToBank()) {
				for (BotInventoryItem item : inventoryItems) {
					if (!bankDAO.deposit(item)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		return true;
	}

	protected final boolean isDeposit(CustomCondition customCondition) {
		return characterService.getInventoryIgnoreEmpty().stream().anyMatch(bii -> customCondition.accept(bii));
	}

}
