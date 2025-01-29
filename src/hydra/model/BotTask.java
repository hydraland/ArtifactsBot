package hydra.model;

import java.io.Serializable;

public final class BotTask implements Serializable {
	private static final long serialVersionUID = 1L;
	private String code;
	private int total;
	private BotTaskType type;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public BotTaskType getType() {
		return type;
	}

	public void setType(BotTaskType type) {
		this.type = type;
	}
}
