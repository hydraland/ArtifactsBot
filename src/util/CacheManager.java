package util;

public interface CacheManager<K, V> {

	void add(K key, V value);
	
	V get(K key);

	boolean contains(K key);

	void clear();
}