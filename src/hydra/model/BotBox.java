package hydra.model;

import java.io.Serializable;

public class BotBox implements Serializable {
	private static final long serialVersionUID = 1L;
	private BoxContent content;
	private int x;
	private int y;

	public BoxContent getContent() {
		return content;
	}

	public void setContent(BoxContent content) {
		this.content = content;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

}
