package hydra.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BotItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private String code;
	private int quantity;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

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
