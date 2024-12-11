package strategy.util.fight.factory;

import java.util.List;
import java.util.Map;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.dao.simulate.StopSimulationException;
import hydra.model.BotCharacter;
import hydra.model.BotEffect;
import hydra.model.BotItem;
import hydra.model.BotItemReader;
import strategy.SumAccumulator;
import strategy.util.CharacterService;
import strategy.util.MoveService;
import strategy.util.fight.HPRecovery;
import strategy.util.fight.RestoreStruct;

public class HPRecoveryUseSimulator implements HPRecovery {
	private final CharacterDAO characterDao;
	private final CharacterService characterService;
	private final ItemDAO itemDAO;
	private final SimulatorManager simulatorManager;
	private final SumAccumulator accumulator;
	private final BankDAO bankDAO;
	private final MoveService moveService;

	public HPRecoveryUseSimulator(CharacterDAO characterDao, ItemDAO itemDAO, BankDAO bankDAO, MoveService moveService,
			CharacterService characterService, SimulatorManager simulatorManager) {
		this.characterDao = characterDao;
		this.itemDAO = itemDAO;
		this.bankDAO = bankDAO;
		this.moveService = moveService;
		this.characterService = characterService;
		this.simulatorManager = simulatorManager;
		accumulator = new SumAccumulator();
	}

	@Override
	public boolean restoreHP(Map<String, Integer> reservedItems) {
		BotCharacter character = characterDao.getCharacter();
		int hpToHeal = character.getMaxHp() - character.getHp();
		if (hpToHeal == 0) {
			return true;
		}
		// Utilisation de la nourriture si possible en ignorant la nourriture réservée
		List<RestoreStruct> healItems = getHealItems(reservedItems, bankDAO, character.getLevel(), itemDAO,
				characterService.getInventoryIgnoreEmpty());

		for (RestoreStruct healItem : healItems) {
			int singleHeal = getHealValue(healItem);
			int quantity;
			if (singleHeal * healItem.quantity() <= hpToHeal) {
				quantity = healItem.quantity();
			} else {
				quantity = hpToHeal / singleHeal + 1;
			}
			if (!characterDao.use(healItem.itemDetails().getCode(), quantity).ok()) {
				return false;
			}
			hpToHeal -= singleHeal * quantity;
			if (hpToHeal <= 0) {
				return true;
			}
		}

		hpToHeal = moveInBankIfMoreFast(hpToHeal, reservedItems);

		if (hpToHeal > 0) {
			return characterDao.rest().ok();
		}
		return true;
	}

	private int moveInBankIfMoreFast(int hpToHeal, Map<String, Integer> reservedItems) {
		simulatorManager.getSimulatorListener()
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));
		simulatorManager.setValue(characterDao.getCharacter(), bankDAO.viewItems());
		accumulator.reset();
		accumulator.setMax(Integer.MAX_VALUE);
		simulatorManager.getCharacterDAOSimulator().rest();
		int maxTime = accumulator.get();
		accumulator.reset();
		accumulator.setMax(maxTime);
		try {
			int hpToHealAfterBank = useRestoreBankItem(hpToHeal, reservedItems, simulatorManager.getMoveService(),
					simulatorManager.getBankDAOSimulator(), simulatorManager.getCharacterDAOSimulator(),
					simulatorManager.getItemDAOSimulator());
			if (hpToHealAfterBank == hpToHeal) {
				return hpToHeal;
			}
		} catch (StopSimulationException sse) {
			return hpToHeal;
		}

		return useRestoreBankItem(hpToHeal, reservedItems, moveService, bankDAO, characterDao, itemDAO);
	}

	private static int useRestoreBankItem(int hpToHeal, Map<String, Integer> reservedItems, MoveService moveService,
			BankDAO bankDAO, CharacterDAO characterDAO, ItemDAO itemDAO) {
		moveService.moveToBank();
		List<RestoreStruct> healItems = getHealItems(reservedItems, bankDAO, characterDAO.getCharacter().getLevel(),
				itemDAO, bankDAO.viewItems());

		for (RestoreStruct healItem : healItems) {
			int singleHeal = getHealValue(healItem);
			int quantity;
			if (singleHeal * healItem.quantity() <= hpToHeal) {
				quantity = healItem.quantity();
			} else {
				quantity = hpToHeal / singleHeal + 1;
			}
			if (!bankDAO.withdraw(restoreStructToBotItem(healItem, quantity))
					&& !characterDAO.use(healItem.itemDetails().getCode(), quantity).ok()) {
				return hpToHeal;
			}
			hpToHeal -= singleHeal * quantity;
			if (hpToHeal <= 0) {
				return 0;
			}
		}
		return hpToHeal;
	}

	private static List<RestoreStruct> getHealItems(Map<String, Integer> reservedItems, BankDAO bankDAO,
			int characterLevel, ItemDAO itemDAO, List<? extends BotItemReader> sourceItems) {
		return sourceItems.stream()
				.filter(bii -> !reservedItems.containsKey(bii.getCode())
						&& characterLevel >= itemDAO.getItem(bii.getCode()).getLevel())
				.map(bii -> new RestoreStruct(itemDAO.getItem(bii.getCode()), bii.getQuantity())).filter(bid -> bid
						.itemDetails().getEffects().stream().anyMatch(bie -> BotEffect.HEAL.equals(bie.getName())))
				.toList();
	}

	private static int getHealValue(RestoreStruct healItem) {
		return healItem.itemDetails().getEffects().stream().filter(bie -> BotEffect.HEAL.equals(bie.getName()))
				.findFirst().get().getValue();
	}

	private static BotItemReader restoreStructToBotItem(RestoreStruct itemStruct, int quantity) {
		BotItem item = new BotItem();
		item.setCode(itemStruct.itemDetails().getCode());
		item.setQuantity(quantity);
		return item;
	}
}