package com.tlc.cache;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Abishek
 * @version 1.0
 */
public interface Cache<K, V>
{
	void put(K key, V data);
	
	V remove(K key);
	
	V get(K key);

	boolean containsKey(K key);
	
	void clear();

	Map<K, V> getAll(Set<K> keys);

	void putAll(Map<K, V> newMap);

	void removeAll(Set<K> keys);

	void forEach(BiConsumer<K, V> consumer);

	V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);
}
