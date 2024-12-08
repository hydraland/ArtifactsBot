package hydra.dao;

import java.util.Collections;
import java.util.List;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.MonstersApi;
import org.openapitools.client.model.DataPageMonsterSchema;
import org.openapitools.client.model.MonsterResponseSchema;

import hydra.dao.util.Convertor;
import hydra.model.BotMonster;
import util.CacheManager;
import util.PermanentCacheManager;

public class MonsterDAOImpl extends AbstractDAO implements MonsterDAO {
	private static final String MONSTERS_KEY_CACHE = "monsters";
	private CacheManager<String, List<BotMonster>> cacheManager;
	private final MonstersApi monstersApi;

	public MonsterDAOImpl(ApiClient apiClient) {
		monstersApi = new MonstersApi(apiClient);
		this.cacheManager = new PermanentCacheManager<>();
	}

	@Override
	public BotMonster getMonster(String code) {
		if (cacheManager.contains(MONSTERS_KEY_CACHE)) {
			return cacheManager.get(MONSTERS_KEY_CACHE).stream().filter(monster -> monster.getCode().equals(code)).findFirst()
					.get();
		} else {
			try {
				ApiResponse<MonsterResponseSchema> response = monstersApi.getMonsterMonstersCodeGetWithHttpInfo(code);
				if (isOk(response)) {
					return Convertor.convert(BotMonster.class, response.getData().getData());
				} else {
					return null;
				}
			} catch (ApiException e) {
				logError(e);
				return null;
			}
		}
	}

	@Override
	public List<BotMonster> getMonsters() {
		if (cacheManager.contains(MONSTERS_KEY_CACHE)) {
			return cacheManager.get(MONSTERS_KEY_CACHE);
		} else {
			try {
				ApiResponse<DataPageMonsterSchema> response = monstersApi.getAllMonstersMonstersGetWithHttpInfo(null,
						null, null, 1, 100);
				if (isOk(response)) {
					List<BotMonster> monsters = response.getData().getData().stream()
							.map(monster -> Convertor.convert(BotMonster.class, monster)).toList();
					cacheManager.add(MONSTERS_KEY_CACHE, monsters);
					return monsters;
				} else {
					return Collections.emptyList();
				}
			} catch (ApiException e) {
				logError(e);
				return Collections.emptyList();
			}
		}
	}
}
