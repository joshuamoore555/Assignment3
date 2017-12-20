package Q2;/*
 * CoarseGrainHashMap.java
 *
 * (C) Hans Vandierendonck 2017
 */
import Q2.Map;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CoarseGrainHashMap<K,V> implements Map<K,V> {
    private final HashMap<K,V> map;
    private final Lock lock = new ReentrantLock();

    CoarseGrainHashMap( int capacity ) {
	map = new HashMap<>(capacity);
    }

    @Override
    public boolean add(K key, V value) {
        synchronized(lock){
            if (key == null) {
                return false;
            } else {
                map.put(key, value);
                return true;
            }
        }
    }

    @Override
    public boolean remove(K key) {
        synchronized (lock) {
            if (key == null) {
                return false;
            } else {
                map.remove(key);
                return true;
            }
        }
    }

    @Override
    public boolean contains(K key) {
        synchronized (lock) {
            if (key == null) {
                return false;
            } else {
                return map.get(key) != null;
            }
        }
    }
    @Override
    public V get(K key) {
        synchronized (lock) {
            if (key == null) {
                return null;
            } else {
                return map.get(key);
            }
        }
    }

    @Override
    public int debuggingCountElements() {
        return map.size();
    }
}
