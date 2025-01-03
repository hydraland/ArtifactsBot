package hydra.dao;

import hydra.dao.response.CraftResponse;
import hydra.dao.response.DeleteItemResponse;
import hydra.dao.response.EquipResponse;
import hydra.dao.response.FightResponse;
import hydra.dao.response.GatheringResponse;
import hydra.dao.response.MoveResponse;
import hydra.dao.response.RecycleResponse;
import hydra.dao.response.RestResponse;
import hydra.dao.response.UseResponse;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotInventoryItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemReader;

public interface CharacterDAO {

	MoveResponse move(int x, int y);

	FightResponse fight();

	DeleteItemResponse deleteItem(BotItemReader item);

	default EquipResponse equip(BotInventoryItem item, BotCharacterInventorySlot slot, int quantity) {
		return equip(item.getCode(), slot, quantity);
	}

	default EquipResponse equip(BotItemDetails item, BotCharacterInventorySlot slot, int quantity) {
		return equip(item.getCode(), slot, quantity);
	}

	default EquipResponse equip(BotItemReader item, BotCharacterInventorySlot slot, int quantity) {
		return equip(item.getCode(), slot, quantity);
	}

	EquipResponse equip(String code, BotCharacterInventorySlot slot, int quantity);

	CraftResponse craft(String code, int quantity);

	default RecycleResponse recycle(BotItemReader item) {
		return recycle(item.getCode(), item.getQuantity());
	}

	default RecycleResponse recycle(BotInventoryItem item) {
		return recycle(item.getCode(), item.getQuantity());
	}

	RecycleResponse recycle(String code, int quantity);

	GatheringResponse collect();

	EquipResponse unequip(BotCharacterInventorySlot slot, int quantity);

	BotCharacter getCharacter();

	RestResponse rest();

	UseResponse use(String code, int quantity);

	default UseResponse use(BotItemReader item) {
		return use(item.getCode(), item.getQuantity());
	}

	default UseResponse use(BotInventoryItem item) {
		return use(item.getCode(), item.getQuantity());
	}
}