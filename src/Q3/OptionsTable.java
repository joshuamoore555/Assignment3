package Q3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// You may modify this class
class OptionsTable {
    private final Map<String, LinkedList<Option>> table = new HashMap<String, LinkedList<Option>>();

    void createSymbol( String symbol ) {
        table.put( symbol, new LinkedList<Option>() );
    }

    void createOption( String symbol, double value, double strike, double deadline, double volatility, char type ) {
        LinkedList<Option> lst = table.get( symbol );
        if( lst == null ) {
            lst = new LinkedList<Option>();
            lst.add( new Option( value, strike, deadline, volatility, type ) );
            table.put( symbol, lst );
        } else {
            lst.add( new Option( value, strike, deadline, volatility, type ) );
        }
    }

    List<Option> get(String symbol ) {
        return table.get( symbol );
    }
}