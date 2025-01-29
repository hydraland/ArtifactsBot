package util;

import java.util.HashMap;
import java.util.Map;

public final class PermanentCacheManager<K, V> implements CacheManager<K, V> {

	private final Map<K, V> cache;

	public PermanentCacheManager() {
		cache = new HashMap<>();
	}
	
	@Override
	public void add(K key, V value) {
		cache.put(key, value);
	}

	@Override
	public V get(K key) {
		return cache.get(key);
	}

	@Override
	public boolean contains(K key) {
		return cache.containsKey(key);
	}

	@Override
	public void clear() {
		cache.clear();
	}

}
