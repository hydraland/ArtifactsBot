package hydra.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BotDropReceived extends BotDrop {
	private static final long serialVersionUID = 1L;
	private int quantity;

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
