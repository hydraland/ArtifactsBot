package strategy.achiever.factory.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemReader;
import strategy.util.CharacterService;
import strategy.util.GraphProcessor;
import strategy.util.MoveService;
import strategy.util.ResourceGraph;

public final class UselessResourceManagerGoalAchiever extends AbstractCustomGoalAchiever {
	private long oldCall;
	private static final long ONE_DAY = 1000 * 60 * 60 * 24l;
	private final ResourceGraph resourceGraph;
	private final Map<String, Map<BotCraftSkill, Integer>> cache;
	private final CharacterDAO characterDAO;
	private final List<String> rareResourceItems;
	private final BankDAO bankDAO;
	private final MoveService moveService;
	private static final List<String> SEARCH_SUB_TYPE = Arrays.asList("mining", "mob", "bar", "plank", "woodcutting");
	private static final float MIN_FREE_SPACE_PER_CENT = 0.2f;
	private final ItemDAO itemDAO;

	public UselessResourceManagerGoalAchiever(CharacterDAO characterDAO, BankDAO bankDAO, ItemDAO itemDAO,
			CharacterService characterService, MoveService moveService, List<String> rareResourceItems) {
		super(characterService);
		this.characterDAO = characterDAO;
		this.bankDAO = bankDAO;
		this.itemDAO = itemDAO;
		this.moveService = moveService;
		this.rareResourceItems = rareResourceItems;
		resourceGraph = new ResourceGraph(itemDAO);
		cache = new HashMap<>();
		oldCall = 0;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		int maxSlot = bankDAO.getBankDetail().getSlots();
		int freeBankSpace = maxSlot - bankDAO.viewItems().size();
		return maxSkillLevel() > GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP
				&& System.currentTimeMillis() - oldCall > ONE_DAY
				&& freeBankSpace <= Math.round(maxSlot * MIN_FREE_SPACE_PER_CENT);
	}

	@Override
	public boolean execute() {
		List<BotItemReader> uselessItems = new ArrayList<>();
		List<? extends BotItemReader> resourceInBank = bankDAO.viewItems().stream()
				.filter(bi -> SEARCH_SUB_TYPE.contains(itemDAO.getItem(bi.getCode()).getSubtype())
						&& !rareResourceItems.contains(bi.getCode()))
				.toList();
		for (BotItemReader botItem : resourceInBank) {
			Map<BotCraftSkill, Integer> result = cache.computeIfAbsent(botItem.getCode(),
					code -> resourceGraph.process(code, new SkillLevelProcessor()));
			if (isUseless(result)) {
				uselessItems.add(botItem);
			}
		}

		if (!uselessItems.isEmpty()) {
			if (moveService.moveToBank()) {
				for (BotItemReader uselessItem : uselessItems) {
					BotItem itemToRemove = new BotItem();
					itemToRemove.setCode(uselessItem.getCode());
					itemToRemove
							.setQuantity(Math.min(uselessItem.getQuantity(), characterService.getFreeInventorySpace()));
					if (!bankDAO.withdraw(itemToRemove) || !characterDAO.deleteItem(itemToRemove).ok()) {
						return false;
					}
				}
				// On met à jour que si le résultat est ok
				oldCall = System.currentTimeMillis();
				return true;
			}
			return false;
		}
		// On met à jour que si le résultat est ok
		oldCall = System.currentTimeMillis();
		return true;
	}

	private boolean isUseless(Map<BotCraftSkill, Integer> itemStat) {
		for (Entry<BotCraftSkill, Integer> entry : itemStat.entrySet()) {
			if (entry.getValue() + GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP > characterService
					.getLevel(entry.getKey())) {
				return false;
			}
		}
		return true;
	}

	private static final class SkillLevelProcessor implements GraphProcessor<Map<BotCraftSkill, Integer>> {

		private Map<BotCraftSkill, Integer> maxLevelCraftSkill;

		@Override
		public void initialize() {
			maxLevelCraftSkill = new EnumMap<>(BotCraftSkill.class);
		}

		@Override
		public GraphProcessor<Map<BotCraftSkill, Integer>> create() {
			return new SkillLevelProcessor();
		}

		@Override
		public void compute(Map<BotCraftSkill, Integer> value) {
			for (Entry<BotCraftSkill, Integer> entry : value.entrySet()) {
				Integer max = maxLevelCraftSkill.get(entry.getKey());
				if (max == null || max < entry.getValue()) {
					maxLevelCraftSkill.put(entry.getKey(), entry.getValue());
				}
			}
		}

		@Override
		public Map<BotCraftSkill, Integer> getResult() {
			return maxLevelCraftSkill;
		}

		@Override
		public void process(BotItemDetails itemDetails, boolean root) {
			if (!root) {
				maxLevelCraftSkill.computeIfAbsent(itemDetails.getCraft().getSkill(), t -> itemDetails.getLevel());
			}
		}
	}

	private int maxSkillLevel() {
		int max = 1;
		BotCraftSkill[] botChraftSkills = BotCraftSkill.values();
		for (int i = 0; i < botChraftSkills.length; i++) {
			int skillLevel = characterService.getLevel(botChraftSkills[i]);
			if (skillLevel > max) {
				max = skillLevel;
			}
		}
		return max;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
