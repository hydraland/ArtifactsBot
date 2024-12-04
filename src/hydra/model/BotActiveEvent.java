package hydra.model;

import java.io.Serializable;

public class BotActiveEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private BotBox map;
	private int duration;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BotBox getMap() {
		return map;
	}

	public void setMap(BotBox map) {
		this.map = map;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
}
