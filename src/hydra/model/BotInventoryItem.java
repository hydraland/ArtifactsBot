package hydra.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BotInventoryItem implements Serializable, BotItemReader {
	private static final long serialVersionUID = 1L;
	private int slot;
	private String code;
	private int quantity;

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

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
		builder.append("slot", slot);
		builder.append("code", code);
		builder.append("quantity", quantity);
		return builder.toString();
	}
}
