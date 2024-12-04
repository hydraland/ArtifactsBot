package hydra.dao.simulate;

import java.io.Serializable;
import java.util.Map;

import hydra.model.BotBankDetail;

public final class BankStruct implements Serializable {

	private static final long serialVersionUID = 1L;
	private BotBankDetail bankDetail;
	private Map<String, Integer> stock;

	public BankStruct() {
	}
	
	public BankStruct(BotBankDetail bankDetail, Map<String, Integer> stock) {
		this.bankDetail = bankDetail;
		this.stock = stock;
	}

	public final BotBankDetail getBankDetail() {
		return bankDetail;
	}

	public final void setBankDetail(BotBankDetail bankDetail) {
		this.bankDetail = bankDetail;
	}

	public final Map<String, Integer> getStock() {
		return stock;
	}

	public final void setStock(Map<String, Integer> stock) {
		this.stock = stock;
	}

}