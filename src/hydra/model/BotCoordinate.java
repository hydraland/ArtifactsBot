package hydra.model;

import java.io.Serializable;

public class BotCoordinate implements Serializable {
	private static final long serialVersionUID = 1L;
	private int x;
	private int y;

	public final int getX() {
		return x;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final int getY() {
		return y;
	}

	public final void setY(int y) {
		this.y = y;
	}
}
