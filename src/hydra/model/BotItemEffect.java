package hydra.model;

import java.io.Serializable;

public class BotItemEffect implements Serializable {
	private static final long serialVersionUID = 1L;
	private BotEffect name;
	private int value;

	public final BotEffect getName() {
		return name;
	}

	public final void setName(BotEffect name) {
		this.name = name;
	}

	public final int getValue() {
		return value;
	}

	public final void setValue(int value) {
		this.value = value;
	}
}
