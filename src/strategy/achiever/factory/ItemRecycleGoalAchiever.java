package strategy.achiever.factory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotInventoryItem;
import strategy.achiever.GoalAchiever;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class ItemRecycleGoalAchiever implements GoalAchiever {

	private final String code;
	private final CharacterDAO characterDAO;
	private boolean finish;
	private final int minPreserve;
	private final MoveService moveService;
	private final BotCraftSkill botCraftSkill;
	private final CharacterService characterService;

	public ItemRecycleGoalAchiever(String code, CharacterDAO characterDAO, MoveService moveService,
			CharacterService characterService, BotCraftSkill botCraftSkill, int minPreserve) {
		this.code = code;
		this.moveService = moveService;
		this.characterDAO = characterDAO;
		this.characterService = characterService;
		this.botCraftSkill = botCraftSkill;
		this.minPreserve = minPreserve;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return characterService.getFirstEquipementInInventory(Arrays.asList(code)).isPresent();
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		try {
			Optional<BotInventoryItem> firstEquipementInInventory = characterService
					.getFirstEquipementInInventory(Arrays.asList(code));
			if (firstEquipementInInventory.isPresent()) {
				BotInventoryItem botInventoryItem = firstEquipementInInventory.get();
				int nbEquiped = (int) characterService.getNoPotionEquipedItems().stream().filter(code::equals).count();
				// On conserve au moins minPreserve exemplaire
				botInventoryItem.setQuantity(nbEquiped + botInventoryItem.getQuantity() - minPreserve);
				if (botInventoryItem.getQuantity() <= 0) {
					return true;
				}
				return moveService.moveTo(botCraftSkill) && characterDAO.recycle(botInventoryItem).ok();
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
		finish = false;
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
		builder.append("code", code);
		builder.append("minPreserve", minPreserve);
		builder.append("botCraftSkill", botCraftSkill);
		return builder.toString();
	}
}
