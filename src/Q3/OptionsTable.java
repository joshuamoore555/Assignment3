package Q3;

import Q2.SegmentedHashMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// You may modify this class
class OptionsTable {
    private final SegmentedHashMap<String, LinkedList<Option>> table = new SegmentedHashMap<String, LinkedList<Option>>(32,111);

    void createSymbol( String symbol ) {
        table.add( symbol, new LinkedList<Option>() );
    }

    void createOption( String symbol, double value, double strike, double deadline, double volatility, char type ) {
        LinkedList<Option> lst = table.get( symbol );
        if( lst == null ) {
            lst = new LinkedList<Option>();
            lst.add( new Option( value, strike, deadline, volatility, type ) );
            table.add( symbol, lst );
        } else {
            lst.add( new Option( value, strike, deadline, volatility, type ) );
        }
    }

    List<Option> get(String symbol ) {
        return table.get( symbol );
    }
}