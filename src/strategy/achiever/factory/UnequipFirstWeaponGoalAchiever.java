package strategy.achiever.factory;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.GameConstants;
import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import strategy.achiever.factory.util.Cumulator;
import strategy.achiever.factory.util.SlotMethod;

public class UnequipFirstWeaponGoalAchiever implements ResourceGoalAchiever {

	private CharacterDAO characterDAO;
	private SlotMethod method;
	private boolean finish;
	private BotCharacterInventorySlot slot;

	public UnequipFirstWeaponGoalAchiever(CharacterDAO characterDAO, SlotMethod method,
			BotCharacterInventorySlot weapon) {
		this.characterDAO = characterDAO;
		this.method = method;
		this.slot = weapon;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return GameConstants.FIRST_WEAPON.equals(method.getValue());// c'est la première arme
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			String code = method.getValue();
			reservedItems.put(code, 1);
			return this.characterDAO.unequip(slot, 1).ok();
		} finally {
			finish = true;
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
	public String getCode() {
		return GameConstants.FIRST_WEAPON;
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
		cumulator.addValue(1);
		return coefficient == 1 && cumulator.getValue() <= maxItem;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
