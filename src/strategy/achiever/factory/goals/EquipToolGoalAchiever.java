package strategy.achiever.factory.goals;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotInventoryItem;
import hydra.model.BotItem;
import hydra.model.BotItemReader;
import hydra.model.BotResourceSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.util.ItemService;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public final class EquipToolGoalAchiever implements GoalAchiever {

	private boolean finish;
	private final CharacterDAO characterDao;
	private final BankDAO bankDAO;
	private final BotResourceSkill resourceSkill;
	private final ItemService itemService;
	private BotInventoryItem inventoryItem;
	private String bankCode;
	private final MoveService moveService;
	private final CharacterService characterService;

	public EquipToolGoalAchiever(CharacterDAO characterDAO, BankDAO bankDAO, MoveService moveService,
			CharacterService characterService, ItemService itemService, BotResourceSkill resourceSkill) {
		this.characterDao = characterDAO;
		this.bankDAO = bankDAO;
		this.moveService = moveService;
		this.characterService = characterService;
		this.itemService = itemService;
		this.resourceSkill = resourceSkill;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		searchTool();
		return inventoryItem != null || bankCode != null;
	}

	private void searchTool() {
		int cooldownReduction = 0;
		inventoryItem = null;
		bankCode = null;
		for (BotInventoryItem currentInventoryItem : characterService.getInventoryIgnoreEmpty()) {
			String code = currentInventoryItem.getCode();
			if (itemService.isTools(code, resourceSkill) && itemService.getToolValue(code) < cooldownReduction) {
				inventoryItem = currentInventoryItem;
				cooldownReduction = itemService.getToolValue(code);
			}
		}
		for (BotItemReader item : bankDAO.viewItems()) {
			String code = item.getCode();
			if (itemService.isTools(code, resourceSkill) && itemService.getToolValue(code) < cooldownReduction) {
				bankCode = code;
				inventoryItem = null;
				cooldownReduction = itemService.getToolValue(code);
			}
		}
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			BotCharacter character = characterDao.getCharacter();
			// TODO Hypothèse c'est que les tools sont que des weapon
			if (itemService.isTools(character.getWeaponSlot(), resourceSkill)) {
				return true;// Déja équipé
			}
			searchTool();// On met à jour en cas de changement
			if (inventoryItem == null && bankCode == null) {
				return false;// On n'a rien à équiper
			}
			BotCharacterInventorySlot slot = inventoryItem != null
					? ItemService.typeToSlot(itemService.getToolType(inventoryItem.getCode()))
					: ItemService.typeToSlot(itemService.getToolType(bankCode));
			if (!"".equals(CharacterService.getSlotValue(character, slot)) && !characterDao.unequip(slot, 1).ok()) {
				return false;
			}

			if (inventoryItem != null) {
				return characterDao.equip(inventoryItem, slot, 1).ok();
			}

			if (moveService.moveToBank()) {
				BotItem botItem = new BotItem();
				botItem.setCode(bankCode);
				botItem.setQuantity(1);
				if (!bankDAO.withdraw(botItem)) {
					return false;
				}
				return characterDao.equip(botItem, slot, 1).ok();
			}
			return false;
		} finally {
			finish = true;
		}
	}

	@Override
	public boolean isFinish() {
		return this.finish;
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
