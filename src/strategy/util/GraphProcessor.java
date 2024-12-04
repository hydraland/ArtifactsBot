package strategy.util;

import hydra.model.BotItemDetails;

public interface GraphProcessor<T> {

	void initialize();

	GraphProcessor<T> create();

	void compute(T value);

	T getResult();

	void process(BotItemDetails itemDetails, boolean root);

}
