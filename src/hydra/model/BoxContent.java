package hydra.model;

import java.io.Serializable;

public final class BoxContent implements Serializable {
	private static final long serialVersionUID = 1L;
	//TODO mettre en enum
	private String code;
	private String type;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
