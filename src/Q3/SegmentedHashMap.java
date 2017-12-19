/*
 * SegmentedHashMap.java
 *
 * (C) Hans Vandierendonck, 2017
 */
package Q3;
import java.util.HashMap;

class SegmentedHashMap<K,V> implements Map<K,V> {
    private final HashMap<K,V>[] segments;
    private final int num_segments;

    SegmentedHashMap( int numseg, int capacity ) {
	num_segments = 0;
	segments = null;
    }

    // Select a segment by hashing the key to a value in the range
    // 0 ... num_segments-1. Base yourself on k.hashCode().
    private int hash( K k ) {
	return 0;
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
