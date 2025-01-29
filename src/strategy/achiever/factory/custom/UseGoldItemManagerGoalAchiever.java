package strategy.achiever.factory.custom;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import hydra.model.BotInventoryItem;
import strategy.util.CharacterService;

public final class UseGoldItemManagerGoalAchiever extends AbstractCustomGoalAchiever {

	private final CharacterDAO characterDAO;
	private final List<String> goldResourceItems;

	public UseGoldItemManagerGoalAchiever(CharacterDAO characterDAO, CharacterService characterService, List<String> goldResourceItems) {
		super(characterService);
		this.characterDAO = characterDAO;
		this.goldResourceItems = goldResourceItems;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return characterService.getInventoryIgnoreEmpty().stream().anyMatch(bii -> goldResourceItems.contains(bii.getCode()));
	}

	@Override
	public boolean execute() {
		for (BotInventoryItem botInventoryItem : characterService.getInventoryIgnoreEmpty()) {
			if (goldResourceItems.contains(botInventoryItem.getCode()) && !characterDAO.use(botInventoryItem).ok()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
