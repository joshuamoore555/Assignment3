package Q3;
// Do not modify this class.
class Event {
    static final int TERMINATE = 0;
    static final int TRADE = 1;
    static final int ADDSYM = 2;
    static final int RMVSYM = 3;
    final int type;
    final long timestamp;
    final String symbol;
    final double bidPrice;
    final double askPrice;

    Event() {
        this.type = TERMINATE;
        this.symbol = null;
        this.bidPrice = this.askPrice = this.timestamp = 0;
    }

    Event( int type, long timestamp, String symbol ) {
        this.type = type;
        this.timestamp = timestamp;
        this.symbol = symbol;
        this.bidPrice = this.askPrice = 0;
    }

    Event( int type, long timestamp, String symbol, double bidPrice, double askPrice ) {
        this.type = type;
        this.timestamp = timestamp;
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(!Event.class.isAssignableFrom(o.getClass()))
            return false;

        final Event e = (Event) o;

        return (this.symbol.equals(e.symbol) && this.type == e.type);
    }

    @Override
    public String toString() {
        return ("Symbol: " + symbol +", BidPrice: " + bidPrice + ", AskPrice: " + askPrice);
    }
}