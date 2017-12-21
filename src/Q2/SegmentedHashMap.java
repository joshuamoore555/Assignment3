package Q2;/*
 * SegmentedHashMap.java
 *
 * (C) Hans Vandierendonck, 2017
 */
import java.util.HashMap;

public class SegmentedHashMap<K,V> implements Map<K,V> {
    private final HashMap<K,V>[] segments;
    private final int num_segments;

    public SegmentedHashMap(int numseg, int capacity) {
	    num_segments = numseg;
        segments = new HashMap[num_segments];
        for(int i = 0; i < num_segments; i++){
            segments[i] = new HashMap<>();
        }
    }

    // Select a segment by hashing the key to a value in the range
    // 0 ... num_segments-1. Base yourself on k.hashCode().
    private int hash(K key) {
        return Math.abs(key.hashCode() % num_segments-1);
    }

    public boolean add(K key, V value) {
        int hashIndex = hash(key);
        HashMap<K,V> segment = segments[hashIndex];
            if(key == null){
                return false;
            }
            else{
                synchronized (segment) {
                    segment.put(key, value);
                    return true;
                }
            }
    }
    
    public boolean remove(K key) {
        int hashIndex = hash(key);
        HashMap<K,V> segment = segments[hashIndex];
            if (key == null) {
                return false;
            } else {
                synchronized (segment) {
                    segment.remove(key);
                    return true;
                }
         }
    }
    
    public boolean contains(K key) {
        int hashIndex = hash(key);
        HashMap<K, V> segment = segments[hashIndex];
        synchronized (segment) {
            return key != null && segment.get(key) != null;
        }
    }
    
    public V get(K key) {
        int hashIndex = hash(key);
        HashMap<K, V> segment = segments[hashIndex];
            if (key == null) {
                return null;
            } else {
                synchronized (segment) {
                 return segment.get(key);
            }
        }
    }

    public int debuggingCountElements() {
        int count = 0;
        for (HashMap e : segments) {
            count += e.size();
        }
        return count;
    }
}
