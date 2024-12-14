package hydra.model;

import java.io.Serializable;

public interface BotItemReader extends Serializable {

	String getCode();

	int getQuantity();

}