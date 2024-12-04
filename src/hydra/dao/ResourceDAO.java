package hydra.dao;

import java.util.List;

import hydra.model.BotResource;

public interface ResourceDAO {

	List<BotResource> getAllResources();
	
	BotResource getResource(String code);
}