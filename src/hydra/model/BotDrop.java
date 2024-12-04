package hydra.model;

import java.io.Serializable;

public abstract class BotDrop implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String code;

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
