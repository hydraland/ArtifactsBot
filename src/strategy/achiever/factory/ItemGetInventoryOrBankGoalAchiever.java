package strategy.achiever.factory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCharacter;
import hydra.model.BotInventoryItem;
import strategy.achiever.factory.util.Cumulator;
import strategy.util.CharacterService;

public class ItemGetInventoryOrBankGoalAchiever implements ArtifactGoalAchiever {

	private boolean finish;
	private String code;
	private final ItemGetBankGoalAchiever itemGetBankGoalAchiever;
	private int quantity;
	private int coefficient;
	private final CharacterService characterService;

	public ItemGetInventoryOrBankGoalAchiever(CharacterService characterService, String code,
			ItemGetBankGoalAchiever itemGetBankGoalAchiever, int quantity) {
		this.characterService = characterService;
		this.itemGetBankGoalAchiever = itemGetBankGoalAchiever;
		itemGetBankGoalAchiever.setInnerUse();
		this.code = code;
		this.finish = false;
		this.quantity = quantity;
		coefficient = 1;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		int currentQuantity = quantity * coefficient;
		Optional<BotInventoryItem> firstEquipementInInventory = characterService
				.getFirstEquipementInInventory(Arrays.asList(code));
		if (firstEquipementInInventory.isPresent()) {
			BotInventoryItem botInventoryItem = firstEquipementInInventory.get();
			if (botInventoryItem.getQuantity() >= currentQuantity) {
				itemGetBankGoalAchiever.setQuantity(0);
				return true;
			} else {
				itemGetBankGoalAchiever.setQuantity(currentQuantity - botInventoryItem.getQuantity());
				return itemGetBankGoalAchiever.isRealisable(character);
			}
		} else {
			itemGetBankGoalAchiever.setQuantity(currentQuantity);
			return itemGetBankGoalAchiever.isRealisable(character);
		}
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		int currentQuantity = quantity * coefficient;
		try {
			int reserveInInventory = ResourceGoalAchiever.reserveInInventory(characterService, code, reservedItems,
					currentQuantity);
			if (reserveInInventory == currentQuantity) {
				return true;
			}
			itemGetBankGoalAchiever.setQuantity(currentQuantity - reserveInInventory);
			return itemGetBankGoalAchiever.execute(reservedItems);
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
	public double getRate() {
		return 1;
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		int currentQuantity = quantity * coefficient;
		if (currentQuantity + cumulator.getValue() <= maxItem) {
			this.coefficient = coefficient;
			cumulator.addValue(currentQuantity);
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
		builder.append("itemGetBankGoalAchiever", itemGetBankGoalAchiever);
		return builder.toString();
	}
}
