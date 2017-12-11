package Q3;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

// Do not modify this class, except for the queueing.
class EventStream {
    private final int replaySpeed;
    private final ArrayDeque<Event> eventQueue;
    private Scanner s;
    private long now = 0;

    EventStream( String filename, int traceTime, int replaySpeed ) {
        eventQueue = new ArrayDeque<Event>();
        this.replaySpeed = replaySpeed;

        long prevTimestamp = 0;
        int elapsedTime = 0;

        try {
            this.s = new Scanner(new File( filename));
            // Format: Type(String) Timestamp(Long) Symbol(String) [BidPrice(double) AskPrice(double) if TRADE]
            while(s.hasNext()) {
                int type = s.nextInt();
                long timestamp = s.nextLong();
                String symbol = s.next();
                Event e = null;
                // System.out.print("type: " + type + " timestamp: " + timestamp + " symbol: " + symbol);
                switch( type ) {
                    case Event.TRADE:
                        double bidPrice = s.nextDouble();
                        double askPrice = s.nextDouble();
                        e = new Event(type, timestamp, symbol, bidPrice, askPrice);
                        // System.out.println(" bidPrice: " + bidPrice + " askPrice: " + askPrice);
                        break;
                    case Event.ADDSYM:
                    case Event.RMVSYM:
                        e = new Event(type, timestamp, symbol);
                        // System.out.println();
                        break;
                    default:
                        e = null;
                        System.err.println("Invalid trace entry type: " + type);
                        System.exit(2);
                }


                eventQueue.add(e);
                if(prevTimestamp > 0) {
                    elapsedTime += timestamp - prevTimestamp;
                }

                prevTimestamp = timestamp;

                // traceTime to MICROSECONDS, stop if exceeded
                if(elapsedTime > traceTime*1e6) {
                    break;
                }
            }
        } catch( FileNotFoundException e ) {
            this.s = null;
            System.err.println( "File not found: " + e );
        }
    }

    Event getEvent() {
        if(eventQueue.isEmpty()) {
            return null;
        }
        Event e = eventQueue.remove();
        if(now > 0) {
            // Use NANOSECONDS to divide by replaySpeed
            long sleepTime = (long) (( e.timestamp - now )*1000.0f / replaySpeed); // ns
            // System.out.println("Sleeping " + sleepTime + " nanoseconds");
            try {
                // Units in NANOSECONDS
                TimeUnit.NANOSECONDS.sleep( sleepTime );
            } catch( InterruptedException ie ) {
                // Got interrupted, oh well ...
            }
        }
        now = e.timestamp;
        return e;
    }
}
