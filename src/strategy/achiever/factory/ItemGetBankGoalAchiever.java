package strategy.achiever.factory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.model.BotCharacter;
import hydra.model.BotInventoryItem;
import hydra.model.BotItem;
import strategy.achiever.factory.util.Cumulator;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class ItemGetBankGoalAchiever implements ArtifactGoalAchiever {

	private boolean finish;
	private final String code;
	private final BankDAO bankDao;
	private int quantity;
	private int coefficient;
	private final CharacterService characterService;
	private boolean root;
	private final MoveService moveService;
	private boolean inner;

	public ItemGetBankGoalAchiever(BankDAO bankDao, String code, MoveService moveService,
			CharacterService characterService) {
		this.bankDao = bankDao;
		this.code = code;
		this.moveService = moveService;
		this.characterService = characterService;
		this.finish = false;
		this.quantity = 1;
		this.coefficient = 1;
		this.root = false;
		inner = false;
	}

	@Override
	public final boolean isRealisable(BotCharacter character) {
		if (root) {
			return false;
		} else if (!inner) {
			return true;
		}
		int currentQuantity = quantity * coefficient;
		BotItem itemInBank = bankDao.getItem(code);
		return itemInBank.getQuantity() >= currentQuantity;
	}

	@Override
	public final boolean execute(Map<String, Integer> reservedItems) {
		if (root) {
			return false;
		}
		int bankQuantity = quantity * coefficient;
		try {
			if (!inner) {
				Optional<BotInventoryItem> firstEquipementInInventory = characterService
						.getFirstEquipementInInventory(Arrays.asList(code));
				if (firstEquipementInInventory.isPresent()) {
					int itemQuantityInInventory = firstEquipementInInventory.get().getQuantity();
					if (itemQuantityInInventory >= bankQuantity) {
						return true;
					} else {
						bankQuantity -= itemQuantityInInventory;
					}
				}
			}

			BotItem searchBankItem = bankDao.getItem(code);
			if (searchBankItem.getQuantity() > 0) {
				if (!moveService.moveToBank()) {
					return false;
				}
				BotItem withdrawItem = new BotItem();
				withdrawItem.setCode(code);
				withdrawItem.setQuantity(Math.min(Math.min(searchBankItem.getQuantity(), bankQuantity),
						characterService.getFreeInventorySpace()));
				if (inner && withdrawItem.getQuantity() < bankQuantity) {
					return false;
				}
				if (inner) {
					ResourceGoalAchiever.reserveItem(code, reservedItems, bankQuantity);
				}
				return (withdrawItem.getQuantity() == 0 || bankDao.withdraw(withdrawItem));
			}
			return true;
		} finally {
			this.finish = true;
		}
	}

	@Override
	public final boolean isFinish() {
		return finish;
	}

	@Override
	public final void clear() {
		this.finish = false;
	}

	@Override
	public void setRoot() {
		this.root = Boolean.TRUE;
	}

	@Override
	public final void unsetRoot() {
		this.root = Boolean.FALSE;
	}

	void setInnerUse() {
		inner = true;
	}

	@Override
	public final double getRate() {
		return 1;
	}

	final String getCode() {
		return code;
	}

	final void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	final int getQuantity() {
		return quantity;
	}

	@Override
	public final boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		int currentQuantity = quantity * coefficient;
		if (currentQuantity + cumulator.getValue() <= maxItem) {
			this.coefficient = coefficient;
			if (inner) {
				cumulator.addValue(currentQuantity);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("code", code);
		builder.append("quantity", quantity);
		builder.append("coefficient", coefficient);
		builder.append("root", root);
		builder.append("inner", inner);
		return builder.toString();
	}
}
