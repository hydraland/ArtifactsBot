package strategy.achiever.factory.util;

import java.util.List;

import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;

public interface GameService {
	boolean isTools(String code);

	boolean isTools(String code, BotResourceSkill botResourceSkill);

	int getToolValue(String code);
	
	BotItemType getToolType(String code);
	
	static BotCharacterInventorySlot typeToSlot(BotItemType type) {
		switch (type) {
		case WEAPON:
			return BotCharacterInventorySlot.WEAPON;
		case AMULET:
			return BotCharacterInventorySlot.AMULET;
		case BODY_ARMOR:
			return BotCharacterInventorySlot.BODY_ARMOR;
		case BOOTS:
			return BotCharacterInventorySlot.BOOTS;
		case HELMET:
			return BotCharacterInventorySlot.HELMET;
		case LEG_ARMOR:
			return BotCharacterInventorySlot.LEG_ARMOR;
		case SHIELD:
			return BotCharacterInventorySlot.SHIELD;
		default:
			throw new IllegalArgumentException("Value  "+type+" not authorize");
		}
	}

	List<String> getToolsCode(BotResourceSkill botResourceSkill);

	List<String> getToolsCode();
}