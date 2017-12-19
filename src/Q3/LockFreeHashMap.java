/*
 * LockFreeHashMap.java
 *
 * Created on November 19, 2017
 *
 */
package Q3;
import java.util.concurrent.atomic.AtomicInteger;


public class LockFreeHashMap<K,V> implements Map<K, V>{
    protected BucketListMap<K,V>[] bucket;
    protected AtomicInteger bucketSize;
    protected AtomicInteger setSize;
    private static final double THRESHOLD = 4.0;
    /**
     * Constructor
     * @param capacity max number of bucket
     */
    public LockFreeHashMap(int capacity) {
	bucket = (BucketListMap<K,V>[]) new BucketListMap[capacity];
	bucket[0] = new BucketListMap<K,V>();
	bucketSize = new AtomicInteger(2);
	setSize = new AtomicInteger(0);
    }

    public boolean add(K k, V v) {
	int myBucket = Math.abs(BucketListMap.hashCode(k) % bucketSize.get());
	BucketListMap<K,V> b = getBucketListMap(myBucket);
	if (!b.add(k, v))
	    return false;
	int setSizeNow = setSize.getAndIncrement();
	int bucketSizeNow = bucketSize.get();
	if (setSizeNow / (double)bucketSizeNow > THRESHOLD
	    && 2 * bucketSizeNow <= bucket.length ) // maximum capacity
	    bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);
	return true;
    }
    /**
     * Remove item from set
     * @param k item to remove
     * @return <code>true</code> iff set changed.
     */
    public boolean remove(K k) {
	int myBucket = Math.abs(BucketListMap.hashCode(k) % bucketSize.get());
	BucketListMap<K, V> b = getBucketListMap(myBucket);
	if (!b.remove(k))
	    return false;		// she's not there
	return true;
    }
    public boolean contains(K k) {
	return get(k) != null;
    }
    public V get(K k) {
	int myBucket = Math.abs(BucketListMap.hashCode(k) % bucketSize.get());
	BucketListMap<K,V> b = getBucketListMap(myBucket);
	return b.get(k);
    }
    private BucketListMap<K,V> getBucketListMap(int myBucket) {
	if (bucket[myBucket] == null)
	    initializeBucket(myBucket);
	return bucket[myBucket];
    }
    private void initializeBucket(int myBucket) {
	int parent = getParent(myBucket);
	if (bucket[parent] == null)
	    initializeBucket(parent);
	BucketListMap<K,V> b = bucket[parent].getSentinel(myBucket);
	if (b != null)
	    bucket[myBucket] = b;
    }
    private int getParent(int myBucket){
	int parent = bucketSize.get();
	do {
	    parent = parent >> 1;
	} while (parent > myBucket);
	parent = myBucket - parent;
	return parent;
    }
    public int debuggingCountElements() {
	int count = 0;
	if( bucket != null )
	    count = bucket[0].debuggingCountElements();
	return count;
    }
}
