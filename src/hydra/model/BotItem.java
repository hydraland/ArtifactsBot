package hydra.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public final class BotItem implements Serializable, BotItemReader {
	private static final long serialVersionUID = 1L;
	private String code;
	private int quantity;

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("code", code);
		builder.append("quantity", quantity);
		return builder.toString();
	}
}
