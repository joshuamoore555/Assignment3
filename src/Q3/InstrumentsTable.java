package Q3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import Q2.SegmentedHashMap;
// You may modify this class
class InstrumentsTable {
    static final int IDLE = 0;
    static final int BUSY = 1;
    static final int LATE = 2;

    // Maps an instrument name to its value
    private SegmentedHashMap<String, AtomicInteger> table = new SegmentedHashMap<String, AtomicInteger>(32,111);

    void create( String symbol ) {
            table.add( symbol, new AtomicInteger( IDLE ) );

    }

    AtomicInteger get( String symbol ) {
            return table.get( symbol );

    }

    boolean update( String symbol, int old_state, int new_state ) {
            AtomicInteger state = get( symbol );
            return state.compareAndSet( old_state, new_state );
    }

    boolean contains( String symbol ) {
            return table.contains( symbol );

    }

    void remove( String symbol ) {

            table.remove ( symbol );

    }
}