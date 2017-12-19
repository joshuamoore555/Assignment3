/*
 * Map.java
 *
 * (C) Hans Vandierendonck, 2017
 */
package Q3;
/**
 * Interface satisfied by various buckets
 * @author Hans Vandierendonck
 */
public interface Map<K,V> {
    /**
     * add object for given key
     * @param k key to add
     * @param v value to add
     * @return whether key was absent
     */
    public boolean add(K k, V v);
    /**
     * remove key from bucket
     * @param k key to remove
     * @return whether key was found
     */
    public boolean remove(K k);
    /**
     * is key in bucket?
     * @param k key being sought
     * @return whether key is present
     */
    public boolean contains(K k);
    /**
     * retrieve value stored for key
     * @param k key being sought
     * @return value if key is present or null
     */
    public V get(K k);
    /**
     * debugging: validate number of elements
     * this method is only called from the main thread
     * in the driver; it need not support concurrent
     * execution.
     * @return number of elements
     */
    public int debuggingCountElements();
}
