package strategy.achiever.factory.custom;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.model.BotCharacter;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public final class DepositGoldInBankGoalAchiever extends AbstractCustomGoalAchiever {

	private static final int MIN_GOLD_TO_DEPOSIT = 2000;
	private static final int MIN_CHARACTER_GOLD = 1000;
	private final CharacterDAO characterDao;
	private final BankDAO bankDAO;
	private final MoveService moveService;

	public DepositGoldInBankGoalAchiever(CharacterDAO characterDao, BankDAO bankDAO, MoveService moveService, CharacterService characterService) {
		super(characterService);
		this.characterDao = characterDao;
		this.bankDAO = bankDAO;
		this.moveService = moveService;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return character.getGold() > MIN_GOLD_TO_DEPOSIT;
	}

	@Override
	public boolean execute() {
		BotCharacter character = characterDao.getCharacter();
		if (character.getGold() > MIN_GOLD_TO_DEPOSIT) {
			int goldQuantity = character.getGold() - MIN_CHARACTER_GOLD;
			return (moveService.moveToBank() && bankDAO.depositGold(goldQuantity));
		}
		return true;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
