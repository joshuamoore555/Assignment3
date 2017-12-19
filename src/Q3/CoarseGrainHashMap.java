/*
 * CoarseGrainHashMap.java
 *
 * (C) Hans Vandierendonck 2017
 */
package Q3;
import java.util.HashMap;

class CoarseGrainHashMap<K,V> implements Map<K,V> {
    private final HashMap<K,V> map;

    CoarseGrainHashMap( int capacity ) {
	map = null;
    }

    public boolean add(K k, V v) {
	return false;
    }
    
    public boolean remove(K k) {
	return false;
    }
    
    public boolean contains(K k) {
	return false;
    }
    
    public V get(K k) {
	return null;
    }

    public int debuggingCountElements() {
	return 0;
    }
}
