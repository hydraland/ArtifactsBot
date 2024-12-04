package hydra.dao.simulate;

import java.io.Serializable;
import java.util.Map;

public final class GrandExchangeStruct implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean sellPossible;
	private Map<String, Integer> estimateItemPrice;
	
	public GrandExchangeStruct() {
	}

	public GrandExchangeStruct(boolean sellPossible, Map<String, Integer> estimateItemPrice) {
		this.sellPossible = sellPossible;
		this.estimateItemPrice = estimateItemPrice;
	}

	public final boolean isSellPossible() {
		return sellPossible;
	}

	public final void setSellPossible(boolean sellPossible) {
		this.sellPossible = sellPossible;
	}

	public final Map<String, Integer> getEstimateItemPrice() {
		return estimateItemPrice;
	}

	public final void setEstimateItemPrice(Map<String, Integer> estimateItemPrice) {
		this.estimateItemPrice = estimateItemPrice;
	}
}