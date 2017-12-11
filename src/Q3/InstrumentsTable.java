package Q3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// You may modify this class
class InstrumentsTable {
    static final int IDLE = 0;
    static final int BUSY = 1;
    static final int LATE = 2;

    // Maps an instrument name to its value
    private Map<String, AtomicInteger> table = new HashMap<String, AtomicInteger>();

    void create( String symbol ) {
        synchronized( table ) {
            table.put( symbol, new AtomicInteger( IDLE ) );
        }
    }

    AtomicInteger get( String symbol ) {
        synchronized( table ) {
            return table.get( symbol );
        }
    }

    boolean update( String symbol, int old_state, int new_state ) {
        synchronized( table ) {
            AtomicInteger state = get( symbol );
            return state.compareAndSet( old_state, new_state );
        }
    }

    boolean contains( String symbol ) {
        synchronized( table ) {
            return table.containsKey( symbol );
        }
    }

    void remove( String symbol ) {
        synchronized( table ) {
            table.remove ( symbol );
        }
    }
}