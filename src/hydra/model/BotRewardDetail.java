package hydra.model;

import java.io.Serializable;

public final class BotRewardDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private String code;
	private int rate;
	private int minQuantity;
	private int maxQuantity;

	public final String getCode() {
		return code;
	}

	public final void setCode(String code) {
		this.code = code;
	}

	public final int getRate() {
		return rate;
	}

	public final void setRate(int rate) {
		this.rate = rate;
	}

	public final int getMinQuantity() {
		return minQuantity;
	}

	public final void setMinQuantity(int minQuantity) {
		this.minQuantity = minQuantity;
	}

	public final int getMaxQuantity() {
		return maxQuantity;
	}

	public final void setMaxQuantity(int maxQuantity) {
		this.maxQuantity = maxQuantity;
	}
}
