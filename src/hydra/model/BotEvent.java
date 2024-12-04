package hydra.model;

import java.io.Serializable;
import java.util.List;

public class BotEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String code;
	private int duration;
	private int rate;
	private BoxContent content;
	private List<BotCoordinate> maps;

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getCode() {
		return code;
	}

	public final void setCode(String code) {
		this.code = code;
	}

	public final int getDuration() {
		return duration;
	}

	public final void setDuration(int duration) {
		this.duration = duration;
	}

	public final int getRate() {
		return rate;
	}

	public final void setRate(int rate) {
		this.rate = rate;
	}

	public final BoxContent getContent() {
		return content;
	}

	public final void setContent(BoxContent content) {
		this.content = content;
	}

	public final List<BotCoordinate> getMaps() {
		return maps;
	}

	public final void setMaps(List<BotCoordinate> maps) {
		this.maps = maps;
	}
}
