package strategy.achiever.factory.custom;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.model.BotBankDetail;
import hydra.model.BotCharacter;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public class ExtendBankSlotGoalAchiever extends AbstractCustomGoalAchiever {

	private final CharacterDAO characterDao;
	private final BankDAO bankDAO;
	private final MoveService moveService;

	public ExtendBankSlotGoalAchiever(CharacterDAO characterDao, BankDAO bankDAO, MoveService moveService, CharacterService characterService) {
		super(characterService);
		this.characterDao = characterDao;
		this.bankDAO = bankDAO;
		this.moveService = moveService;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		BotBankDetail bankDetail = bankDAO.getBankDetail();
		return bankDetail.getNextExpansionCost() <= character.getGold() + bankDetail.getGold();
	}

	@Override
	public boolean execute() {
		BotBankDetail bankDetail = bankDAO.getBankDetail();
		BotCharacter character = characterDao.getCharacter();
		if (character.getGold() < bankDetail.getNextExpansionCost()) {
			int goldQuantity = bankDetail.getNextExpansionCost() - character.getGold();
			if (!moveService.moveToBank() || !bankDAO.withdrawGold(goldQuantity)) {
				return false;
			}
		}
		character = characterDao.getCharacter();
		return (character.getGold() < bankDetail.getNextExpansionCost())
				|| moveService.moveToBank() && bankDAO.buyExtension();
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
