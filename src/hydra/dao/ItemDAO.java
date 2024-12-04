package hydra.dao;

import java.util.List;

import hydra.dao.response.UseInCraftResponse;
import hydra.model.BotCraftSkill;
import hydra.model.BotItemDetails;
import hydra.model.BotItemType;

public interface ItemDAO {

	List<BotItemDetails> getResourceItems();

	List<BotItemDetails> getItems();

	List<BotItemDetails> getItems(BotCraftSkill skill);

	List<BotItemDetails> getItems(BotItemType type);

	List<BotItemDetails> getItems(BotItemType type, Integer minLevel, Integer maxLevel);
	
	List<BotItemDetails> getItems(Integer minLevel, Integer maxLevel);

	List<BotItemDetails> getTaskItems();

	UseInCraftResponse useInCraft(String code);

	BotItemDetails getItem(String code);

}