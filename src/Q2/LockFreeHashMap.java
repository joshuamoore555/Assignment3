package Q2;/*
 * LockFreeHashMap.java
 *
 * Created on November 19, 2017
 *
 */

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Maurice Herlihy
 * @author Hans Vandierendonck
 */
public class LockFreeHashMap<K,V> implements Map<K, V> {
    protected BucketListMap<K,V>[] bucket;
    protected AtomicInteger bucketSize;
    protected AtomicInteger setSize;
    private static final double THRESHOLD = 4.0;
    private volatile int bucketLength = 0;

    /**
     * Constructor
     * @param capacity max number of bucket
     */

    public LockFreeHashMap(int capacity) {
		bucket = (BucketListMap<K,V>[]) new BucketListMap[capacity];
		bucket[0] = new BucketListMap<K,V>();
		bucketSize = new AtomicInteger(2);
		setSize = new AtomicInteger(0);
		bucketLength = bucket.length;
    }

    public boolean add(K key, V value) {
		int myBucket = Math.abs(BucketListMap.hashCode(key) % bucketSize.get());
		BucketListMap<K,V> b = getBucketListMap(myBucket);
		if (b.add(key, value) == false) {
		    System.out.println("Hash Clash");
			return false;
		}
		int setSizeNow = setSize.getAndIncrement();
		int bucketSizeNow = bucketSize.get();
		if(setSizeNow / (double)bucketSizeNow > THRESHOLD && 2 * bucketSizeNow <= bucket.length ) { // maximum capacity
            bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);
        }
		return true;
    }

    public boolean remove(K key) {
        int myBucket = Math.abs(BucketListMap.hashCode(key) % bucketSize.get());
        BucketListMap<K, V> b = getBucketListMap(myBucket);
        return b.remove(key);
    }

    public boolean contains(K key) {
        return get(key) != null;
    }

    public V get(K key) {
        int myBucket = Math.abs(BucketListMap.hashCode(key) % bucketSize.get());
        BucketListMap<K,V> b = getBucketListMap(myBucket);
        return b.get(key);
    }

    private BucketListMap<K,V> getBucketListMap(int myBucket) {
        if (bucket[myBucket] == null) {
            initializeBucket(myBucket);
        }
        return bucket[myBucket];
    }

    private void initializeBucket(int myBucket) {
        int parent = getParent(myBucket);
        if (bucket[parent] == null) {
            initializeBucket(parent);
        }
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

//    public void resize(int length){
//        if(length == bucketLength) {
//            BucketListMap<K, V>[] newBucket = new BucketListMap[bucket.length * 2];
//            bucketLength = newBucket.length;
//            System.arraycopy(bucket, 0, newBucket, 0, bucket.length);
//            bucket = newBucket;
//        }
//    }
}
