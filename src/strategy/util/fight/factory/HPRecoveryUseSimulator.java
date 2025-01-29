package strategy.util.fight.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.dao.simulate.SimulatorManager;
import hydra.dao.simulate.StopSimulationException;
import hydra.model.BotCharacter;
import strategy.SumAccumulator;
import strategy.achiever.factory.util.Coordinate;
import strategy.util.CharacterService;
import strategy.util.MoveService;
import strategy.util.fight.RestoreStruct;

public final class HPRecoveryUseSimulator extends AbstractHPRecovery {
	private final CharacterService characterService;
	private final ItemDAO itemDAO;
	private final SimulatorManager simulatorManager;
	private final SumAccumulator accumulator;
	private final BankDAO bankDAO;
	private final MoveService moveService;

	public HPRecoveryUseSimulator(CharacterDAO characterDao, ItemDAO itemDAO, BankDAO bankDAO, MoveService moveService,
			CharacterService characterService, SimulatorManager simulatorManager) {
		super(characterDao);
		this.itemDAO = itemDAO;
		this.bankDAO = bankDAO;
		this.moveService = moveService;
		this.characterService = characterService;
		this.simulatorManager = simulatorManager;
		accumulator = new SumAccumulator();
	}

	@Override
	protected boolean restoreHP(int hpToHeal, Map<String, Integer> reservedItems, BotCharacter character) {
		int hpToHealAfterInventoryFood = restoreHPWithFoodInInventory(hpToHeal, character, reservedItems, itemDAO,
				characterService);
		if (hpToHealAfterInventoryFood <= 0) {
			return true;
		}
		int hpToHealAfterBankFood = moveInBankIfMoreFast(hpToHealAfterInventoryFood, reservedItems);

		if (hpToHealAfterBankFood > 0) {
			return characterDao.rest().ok();
		}
		return true;
	}

	private int moveInBankIfMoreFast(int hpToHeal, Map<String, Integer> reservedItems) {
		simulatorManager.getSimulatorListener()
				.setInnerListener((className, methodName, cooldown, error) -> accumulator.accumulate(cooldown));
		BotCharacter character = characterDao.getCharacter();
		simulatorManager.setValue(character, bankDAO.viewItems());
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
		BotCharacter character = characterDAO.getCharacter();
		Coordinate characterCoordinate = new Coordinate(character.getX(), character.getY());
		List<RestoreStruct> healItems = getHealItems(reservedItems, character.getLevel(), itemDAO, bankDAO.viewItems());
		try {
			for (RestoreStruct healItem : healItems) {
				int singleHeal = getHealValue(healItem);
				int quantity;
				if (singleHeal * healItem.quantity() <= hpToHeal) {
					quantity = healItem.quantity();
				} else {
					quantity = hpToHeal / singleHeal + 1;
				}
				if (!moveService.moveToBank() || !bankDAO.withdraw(healItem.itemDetails().getCode(), quantity)
						|| !characterDAO.use(healItem.itemDetails().getCode(), quantity).ok()) {
					return hpToHeal;
				}
				hpToHeal -= singleHeal * quantity;
				if (hpToHeal <= 0) {
					return 0;
				}
			}
			return hpToHeal;
		} finally {
			moveService.moveTo(Arrays.asList(characterCoordinate));
		}
	}
}