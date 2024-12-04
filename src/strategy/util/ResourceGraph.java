package strategy.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hydra.dao.ItemDAO;
import hydra.model.BotCraft;
import hydra.model.BotItem;
import hydra.model.BotItemDetails;

public class ResourceGraph {

	private final Map<String, ItemNode> graph;

	public ResourceGraph(ItemDAO itemDAO) {
		graph = new HashMap<>();
		create(itemDAO.getItems());
	}

	private void create(List<BotItemDetails> items) {
		for (BotItemDetails botItemDetails : items) {
			String code = botItemDetails.getCode();
			graph.computeIfAbsent(code, c -> new ItemNode());
			graph.get(code).setValue(botItemDetails);
			BotCraft craft = botItemDetails.getCraft();
			if (craft != null) {
				for (BotItem botItem : craft.getItems()) {
					String itemCode = botItem.getCode();
					graph.computeIfAbsent(itemCode, c -> new ItemNode());
					graph.get(itemCode).addTransition(code, botItem.getQuantity());
				}
			}
		}
	}
	
	public <T> T process(String code, GraphProcessor<T> processor) {
		processor.initialize();
		ItemNode itemNode = graph.get(code);
		processor.compute(process(itemNode, processor.create(), true));
		return processor.getResult();
	}
	
	private <T> T process(ItemNode itemNode, GraphProcessor<T> processor, boolean root) {
		processor.initialize();
		List<ItemTransition> children = itemNode.getChildren();
		if(children != null) {
			for (ItemTransition itemTransition : children) {
				processor.compute(process(graph.get(itemTransition.code()), processor.create(), false));
			}
		}
		processor.process(itemNode.getValue(), root);
		return processor.getResult();
	}

	@Override
	public String toString() {
		return "ResourceGraph [graph=" + graph + "]";
	}
}
