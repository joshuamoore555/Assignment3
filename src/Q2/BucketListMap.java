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

    static public final int WORD_SIZE = 23;
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
		int hash = getHash(key);
		while(true) {
			Window window = find(head, hash);
			Node pred = window.pred;
			Node curr = window.curr;

			if( curr.hash == hash ) {
				return false;
			} else {
                Node node = new Node(hash,key,value);
                node.next = new AtomicMarkableReference<>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {return true;}
			}
		}
	}

	public boolean remove(K key) {
		int hash = getHash(key);
		boolean snip;
		while(true) {
			Window window = find(head, hash);
			Node pred = window.pred;
			Node curr = window.curr;
			if(curr.hash != hash)
				return false;
			else {
                Node succ = curr.next.getReference();
                snip = curr.next.attemptMark(succ, true);
                if (!snip) continue;
                    pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
		}
	}

    public boolean contains(K key) {
		return get(key) != null;
    }

    public V get(K key) {
		int hash = getHash(key);
		Node curr = this.head;
		while(curr.hash < hash){
			curr = curr.getNext();
		}
		return (curr.hash == hash) ? curr.value : null;
    }

    private Node addSentinel(int bucket) {
	int hash = makeSentinelHash( bucket );
	while(true) {
	    Window window = find( head, hash );
	    Node pred = window.pred;
	    Node curr = window.curr;
	    if(curr.hash == hash)
		return curr; // all sentinels are equal
	    else {
		Node node = new Node(hash);
		node.next = new AtomicMarkableReference<>(curr,false);
		pred.next = new AtomicMarkableReference<>(node, false);
		return node; // return the newly created node
	    }
	}
    }

    public static int hashCode(Object x) {
	return x.hashCode() & PRE_MASK;
    }

    private int getHash( K key ) {
	int code = hashCode( key );
	return reverse(code | REG_BIT);
    }

    private int makeSentinelHash(int key) {
	return reverse(key & PRE_MASK);
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

    public BucketListMap<K,V> getSentinel(int index) {
		int key = makeSentinelHash(index);
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
		}}
    
    public int debuggingCountElements() {
	int count = 0;
	Node curr = head.getNext();
	while ( curr != null ) {
	    if( curr.value != null ) // is this a sentinel node?
		++count;
	    curr = curr.getNext();
	}
	return count;
    }

	private class Node {
		K key;
		V value;
		int hash;
		AtomicMarkableReference<Node> next;

		Node(int hash) {
			this.hash = hash;
			this.next  = new AtomicMarkableReference<Node>(null, false);
		}

		Node(int hash, K key, V value) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next  = new AtomicMarkableReference<Node>(null, false);
		}

		Node getNext() {
			boolean[] cMarked = {false};
			boolean[] sMarked = {false};
			Node entry = this.next.get(cMarked);
			while (cMarked[0]) {
				Node succ = entry.next.get(sMarked);
				this.next.compareAndSet(entry, succ, true, sMarked[0]);
				entry = this.next.get(cMarked);
			}
			return entry;
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

	private Window find(Node head, int hash) {
		Node pred = null;
		Node curr = null;
		Node succ = null;

		while(true){
			pred = head;
			curr = pred.getNext();
			while(true){
				succ = curr.getNext();
				if( curr.hash >= hash )
					return new Window(pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}
}
