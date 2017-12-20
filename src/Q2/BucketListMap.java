package Q2;/*
 * BucketListMap.java
 *
 * Created on December 30, 2005, 3:24 PM
 *
 * From "Multiprocessor Synchronization and Concurrent Data Structures",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */


import java.util.concurrent.atomic.AtomicMarkableReference;

public class BucketListMap<K, V> implements Map<K, V> {
    private Node head;

    static public final int WORD_SIZE = 25;
    static private final int LO_BIT = 1;
    static private final int HI_BIT = 1 << (WORD_SIZE - 1);
    static private final int REG_BIT = 1 << WORD_SIZE;
    static private final int PRE_MASK = (1 << (WORD_SIZE - 1)) - 1;

	/**
	 * Constructor
	 */
	public BucketListMap() {
		this.head = new Node( 0 );
		this.head.next = new AtomicMarkableReference<Node>(new Node(Integer.MAX_VALUE), false); //tail
	}

	private BucketListMap(Node e) {
		this.head = e;
	}

	public boolean add(K key, V value) {
		int hash = makeOrdinaryKey(key);
		while(true) {
			Window window = find(head, hash);
			Node pred = window.pred;
			Node curr = window.curr;
			if( curr.hash == hash ){
				return false;
			} else{
			    Node node = new Node(hash, key, value);
			    node.next = new AtomicMarkableReference<>(curr,false);
			    if(pred.next.compareAndSet(curr,node,false,false)) {
                    return true;
                }
			}
		}
	}

	public boolean remove(K key) {
		int hash = makeOrdinaryKey(key);
		boolean snip;
		while(true) {
            Window window = find(head, hash);
            Node pred = window.pred;
            Node curr = window.curr;
            if (hash != curr.hash) {
                return false;
            } else {
                Node succ = curr.next.getReference();
                snip = curr.next.attemptMark(succ, true);
                if (snip == false) {
                    continue;
                }
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
	}

    public boolean contains(K key) {
        boolean[] marked = {false};
        int hash = makeOrdinaryKey(key);
        Node curr = head;
        while(curr.hash < hash){
            curr = curr.next.getReference();
            Node succ = curr.next.get(marked);
        }
        return (curr.hash == hash && !marked[0]);
    }

    public V get(K key) {
		int hash = makeOrdinaryKey(key);
		Node curr = head;
		while(curr.hash < hash){
			curr = curr.next.getReference();
		}
		return (curr.hash == hash) ? curr.value : null;

    }
    
    public int debuggingCountElements() {
	    int count = 0;
        Node curr = head.next.getReference();
        while ( curr != null ) {
            if( curr.value != null) // is this a sentinel node?
            count++;
            curr = curr.next.getReference();
        }
        return count;
    }

    public BucketListMap<K,V> getSentinel(int index) {
        int key = makeSentinelKey(index);
        boolean splice;
        while(true) {
            Window window = find(head, key);
            Node pred = window.pred;
            Node curr = window.curr;

            if (curr.hash == key) {
                return new BucketListMap<>(curr);
            } else {
                Node node = new Node(key);
                node.next.set(pred.next.getReference(), false);
                splice = pred.next.compareAndSet(curr, node, false, false);
                if (splice) {
                    return new BucketListMap<>(node);
                } else {
                    continue;
                }
            }
        }
    }

    private static int reverse(int key) {
        int loMask = LO_BIT;
        int hiMask = REG_BIT;
        int result = 0;
        for (int i = 0; i <= WORD_SIZE; i++) {
            if ((key & loMask) != 0) {  // bit set
                result |= hiMask;
            }
            loMask <<= 1;
            hiMask >>>= 1;  // fill with 0 from left
        }
        return result;
    }

    public static int hashCode(Object x) {
        return x.hashCode() & PRE_MASK;
    }

    public int makeOrdinaryKey(K key ) {
        int code = hashCode( key );
        return reverse(code | REG_BIT);
    }

    private int makeSentinelKey(int key) {
        return reverse(key & PRE_MASK);
    }

	private class Node {
		K key;
		V value;
		int hash;
		AtomicMarkableReference<Node> next;

		Node(int hash) {
			this.hash = hash;
			this.next = new AtomicMarkableReference<Node>(null, false);
		}

		Node(int hash, K key, V value) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = new AtomicMarkableReference<Node>(null, false);
		}
	}

	private class Window {
		public Node pred;
		public Node curr;

		Window(Node pred, Node curr) {
			this.pred = pred;
			this.curr = curr;
		}
	}

	public Window find(Node head, int hash) {
		Node pred = null;
		Node curr = null;
		Node succ = null;

		boolean[] marked = {false};
		boolean snip;

		retry: while(true){
		    pred = head;
		    curr = pred.next.getReference();
		    while(true){
		        succ = curr.next.get(marked);
		        while(marked[0]){
		            snip = pred.next.compareAndSet(curr, succ,false,false);
		            if(snip == false){
		                continue retry;
                    }
                    curr = succ;
		            succ = curr.next.get(marked);
                }
                if(curr.hash >= hash){
		            return new Window(pred, curr);
                }
                pred = curr;
                curr = succ;
            }
        }
	}
}
