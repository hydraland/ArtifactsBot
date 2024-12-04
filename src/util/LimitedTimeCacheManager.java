package util;

import java.util.HashMap;
import java.util.Map;

public class LimitedTimeCacheManager<K, V> implements CacheManager<K, V> {

	private final Map<K, CacheValue<V>> cache;
	private final long maxCacheTimeDuration;

	public LimitedTimeCacheManager(long maxCacheInSecond) {
		this.maxCacheTimeDuration = maxCacheInSecond * 1000;
		cache = new HashMap<>();
	}

	@Override
	public void add(K key, V value) {
		cache.put(key, new CacheValue<>(value, System.currentTimeMillis()));
	}

	@Override
	public V get(K key) {
		// Pour avoir une valeur valide il faut appeler contains avant
		return cache.get(key).value;
	}

	@Override
	public boolean contains(K key) {
		long currentTime = System.currentTimeMillis();
		boolean result = cache.containsKey(key) && (currentTime - cache.get(key).addTime) <= maxCacheTimeDuration;
		if (!result) {
			cache.remove(key);
		}
		return result;
	}

	@Override
	public void clear() {
		cache.clear();
	}

	private record CacheValue<V>(V value, long addTime) {
	}
}
