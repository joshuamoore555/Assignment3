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
    protected BucketListMap<K,V>[] bucket; //bucket of maps
    protected AtomicInteger bucketSize; //max size of the bucket
    protected AtomicInteger numberOfElements; // amount of maps in the bucket
    protected AtomicInteger bucketCapacity; // amount of maps in the bucket

    private static final double THRESHOLD = 4.0;
    private static final double factorLoad = 0.75;

    /**
     * Constructor
     * @param capacity max number of bucket
     */

    public LockFreeHashMap(int capacity) {
        bucket = (BucketListMap<K,V>[]) new BucketListMap[capacity];
        bucket[0] = new BucketListMap<K,V>();
        bucketSize = new AtomicInteger(2);
        numberOfElements = new AtomicInteger(0);
        bucketCapacity = new AtomicInteger(capacity);
    }

    public boolean add(K key, V value) {
        int myBucket = Math.abs(BucketListMap.hashCode(key) % bucketSize.get());
        BucketListMap<K,V> b = getBucketListMap(myBucket);
        if (b.add(key, value) == false) {
            return false;
        }

        int numElements = numberOfElements.getAndIncrement();
        int bucketSizeNow = bucketSize.get();

        System.out.println("Map size and bucket length = " + numElements + " " + bucket.length);

        if(numberOfElements.get() >= bucket.length * factorLoad) {
            //resize
            System.out.println(numberOfElements.get() + " is greater than or equal to " + bucket.length * factorLoad);
            if (bucketCapacity.compareAndSet(bucket.length, bucket.length * 2)) {
                BucketListMap<K, V>[] newBucket = new BucketListMap[bucketCapacity.get()];
                System.arraycopy(bucket, 0, newBucket, 0, bucket.length);
                bucket = newBucket;
            }
        }

        if(numElements/ (double)bucketSizeNow > THRESHOLD && 2 * bucketSizeNow <= bucket.length ) { // maximum capacity
            bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);
        }
        return true;
    }

    public boolean remove(K key) {
        int myBucket = Math.abs(BucketListMap.hashCode(key) % bucketSize.get());
        BucketListMap<K, V> b = getBucketListMap(myBucket);
        if(b.remove(key) == false){
            return false;
        }else {
            return true;
        }
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
}
