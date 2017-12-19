package Q2;
/*
 * PreTrade.java
 *
 * (C) Hans Vandierendonck, Giorgis Georgakoudis, 2017
 */

import java.util.List;
import java.util.LinkedList;
import java.lang.Math;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayDeque;

// Do not modify this class
class Option {
    private double value;
    private final double strike;
    private final double deadline;
    private final double volatility;
    private final char type;

    Option( double value, double strike, double deadline, double volatility, char type) {
	this.value = value;
	this.strike = strike;
	this.deadline = deadline;
	this.volatility = volatility;
	this.type = type;
    }

    double getValue() { return value; }
    void setValue( double value_ ) { value = value_; }

    double getStrike() { return strike; }
    double getDeadline() { return deadline; }
    double getVolatility() { return volatility; }
    char getType() { return type; }
}

// You may modify this class
class OptionsTable {
    private final LockFreeHashMap<String, LinkedList<Option>> table = new LockFreeHashMap<String, LinkedList<Option>>(8192);
    AtomicInteger addCount = new AtomicInteger(0);
    AtomicInteger getCount = new AtomicInteger(0);


    void createSymbol( String symbol ) {
    	addCount.getAndIncrement();
    	//System.out.println("Add Count = " + addCount.get());
	table.add( symbol, new LinkedList<Option>() );
    }

    void createOption( String symbol, double value, double strike, double deadline, double volatility, char type ) {
	LinkedList<Option> lst = table.get( symbol );
        getCount.getAndIncrement();
        //System.out.println("Get Count = " + getCount.get());
	if( lst == null ) {
	    lst = new LinkedList<Option>();
	    lst.add( new Option( value, strike, deadline, volatility, type ) );
        addCount.getAndIncrement();
        //System.out.println("Add Count = " + addCount.get());
	    table.add( symbol, lst );
	} else {
        addCount.getAndIncrement();
        //System.out.println("Add Count = " + addCount.get());
	    lst.add( new Option( value, strike, deadline, volatility, type ) );
	}
    }

    List<Option> get( String symbol ) {
        getCount.getAndIncrement();
        //System.out.println("Get Count = " + getCount.get());
	return table.get( symbol );
    }
}

// You may modify this class
class InstrumentsTable {
    static final int IDLE = 0;
    static final int BUSY = 1;
    static final int LATE = 2;

    AtomicInteger addCount = new AtomicInteger(0);
    AtomicInteger getCount = new AtomicInteger(0);
    AtomicInteger removeCount = new AtomicInteger(0);


    // Maps an instrument name to its value
    private LockFreeHashMap<String, AtomicInteger> table = new LockFreeHashMap<String, AtomicInteger>(8192);

    void create( String symbol ) {
        addCount.getAndIncrement();
        System.out.println("Add Count = " + addCount.get());
	    table.add( symbol, new AtomicInteger( IDLE ) );
    }

    AtomicInteger get( String symbol ) {
        getCount.getAndIncrement();
        //System.out.println("Get Count = " + getCount.get());
	    return table.get( symbol );
    }

    boolean update( String symbol, int old_state, int new_state ) {
	    AtomicInteger state = get( symbol );
	    return state.compareAndSet( old_state, new_state );

    }

    boolean contains( String symbol ) {
        getCount.getAndIncrement();
        //System.out.println("Get Count = " + getCount.get());
	    return table.contains( symbol );

    }

    void remove( String symbol ) {
        removeCount.getAndIncrement();
        System.out.println("Remove Count = " + removeCount.get());
	    table.remove ( symbol );
	}
}


// Do not modify this class
public class OptionPricer {
    public static void main( String args[] ) {
	if(args.length < 7) {
	    System.err.println("Usage: java PreTrade <instruments file> <options file> <trace file> <trace time (s)> <replay speed> <num of calculation steps> <num of threads>");
	    System.exit(1);
	}

	String instrumentsFile = args[0];
	String optionsFile = args[1];
	String traceFile = args[2];
	int traceTime = Integer.parseInt(args[3]);
	int replaySpeed = Integer.parseInt(args[4]);
	int numSteps = Integer.parseInt(args[5]);
	int numThreads = Integer.parseInt(args[6]);

	// Initialise the table of instruments

	// Initialise the options (how many options per symbol?)
	// do we get this from the event stream?

	Valuation val = new Valuation( numSteps, numThreads );

	// Read instruments file
	try {
	    // Format: Symbol(String)
	    Scanner s = new Scanner( new File( instrumentsFile ) );
	    while(s.hasNext()) {
		String symbol = s.next();
		val.createSymbol(symbol);
	    }
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    return;
	}

	// Read options file
	try {
	    // Format: Identifier(String) Symbol(String) Strike(double) Deadline(double) Volatility(double) Type(String)
	    Scanner s = new Scanner( new File( optionsFile ) );
	    while(s.hasNext()) {
		String identifier = s.next();
		String symbol = s.next();
		double strike = s.nextDouble();
		double deadline = s.nextDouble();
		double volatility = s.nextDouble();
		char type = s.next().charAt(0);
		//System.out.println( "symbol: " + symbol + ", strike: " + strike + ", deadline:" + deadline + ", volatility:" + volatility + ", type:" + type);
		val.createOption( symbol, strike, deadline, volatility, type );
	    }
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    return;
	}

	val.start();

	// Event loop
	val.EventLoop( new EventStream( traceFile, traceTime, replaySpeed ) );


	// Output final values of options
    }
}

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
	return new String("symbol:" + symbol +", bidPrice: " + bidPrice + ", askPrice: " + askPrice);
    }
}

// Do not modify this class, except for the queueing.
class EventStream {
    private Scanner s;
    private long now = 0;
    private final int replaySpeed;
    private final ArrayDeque<Event> eventQueue;

    EventStream( String filename, int traceTime, int replaySpeed ) {
	eventQueue = new ArrayDeque<Event>();
	this.replaySpeed = replaySpeed;

	long prevTimestamp = 0;
	int elapsedTime = 0;

	try {
	    this.s = new Scanner( new File( filename) );
	    // Format: Type(String) Timestamp(Long) Symbol(String) [BidPrice(double) AskPrice(double) if TRADE]
	    while( s.hasNext() ) {
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
		if(prevTimestamp > 0)
		    elapsedTime += timestamp - prevTimestamp;

		prevTimestamp = timestamp;

		// traceTime to MICROSECONDS, stop if exceeded
		if(elapsedTime > traceTime*1e6)
		    break;
	    }
	} catch( FileNotFoundException e ) {
	    this.s = null;
	    System.err.println( "File not found: " + e );
	}
    }

    Event getEvent() {
	if(eventQueue.isEmpty())
	    return null;

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

// Do not modify this class, except for the queueing
class ProcessingThread extends Thread {
    final Valuation val;
    final LinkedBlockingQueue<Event> queue;

    ProcessingThread( Valuation val, LinkedBlockingQueue<Event> queue ) {
	this.val = val;
	this.queue = queue;
    }

    public void run() {
	// System.out.println( "Thread running" );
	while( true ) {
	    try {
		Event e = queue.take();
		//System.out.println("Thread " + this.getId() + " took " + e + "  out of the q");
		if( !val.processEvent( e ) )
		    break;
	    } catch( InterruptedException exc ) {
		System.err.println( "Exception during join: " + exc );
	    }
	}
	// System.out.println( "Thread done" );
    }
}

// You may modify certain parts of this class, but you should retain its
// functionality.
class Valuation {
    // Do not modify these variables
    private static final double RISKFREE = 0.03f;
    private final int NUM_STEPS; // command line argument
    private final Thread[] threads;
    private int num_late_analyses;
    private int num_analyses;

    // You may modify these variables
    private final InstrumentsTable instruments;
    private final OptionsTable options;
    private final LinkedBlockingQueue<Event> queue;

    static final boolean verbose = true;
    
    // Do not modify the constructor
    Valuation( int numSteps, int numThreads ) {
	// portfolios = new PortfolioTable();
	instruments = new InstrumentsTable();
	options = new OptionsTable(); //split the options table here
        //put into an array of threads.length

	queue = new LinkedBlockingQueue<Event>();
	num_analyses = 0;
	num_late_analyses = 0;
	NUM_STEPS = numSteps;

	threads = new Thread[numThreads];
    }

    // Do not modify this method
    public void start() {
	for( int i=0; i < threads.length; ++i ) {
	    threads[i] = new ProcessingThread( this, queue ); //send the individual option tables here from the array
	    threads[i].start();
	}
    }

    // You may modify this method but must keep it functionally equivalent.
    // Main event loop, processing trades as they occur in real time.
    // More precisely, we try to mimick real time behavior by reading the
    // events from a trace.
    void EventLoop( EventStream stream ) {
	Event e;
	while( (e = stream.getEvent()) != null ) {
	    // Trade update
	    if( e.type == Event.TRADE ) {
		// If symbol is indeed in instruments, proceed 
		if(instruments.contains( e.symbol )) {
		    num_analyses++;
		    if( verbose )
			System.out.println("EventLoop: " + e.symbol
					   + " " + e.bidPrice + "/" + e.askPrice
					   + ": " + num_analyses + "th trade");

		    // If true, removes any stale pending analysis
		    if(queue.remove(e)) {
			num_late_analyses++;
			if( verbose )
			    System.out.println("EventLoop: " + e.symbol
					       + ": Missed deadline for "
					       + e.symbol + "; "
					       + num_late_analyses + "/"
					       + num_analyses);
		    }
		    // Pricing is in progress, increment late analyses counter
		    // and signal the thread to stop processing this instrument
		    else if( instruments.get( e.symbol ).get()
			     == InstrumentsTable.BUSY ) {
			instruments.update( e.symbol,
					    InstrumentsTable.BUSY,
					    InstrumentsTable.LATE );
			num_late_analyses++;
			if( verbose )
			    System.out.println("EventLoop: " + e.symbol
					       + ": Missed deadline for "
					       + e.symbol + "; "
					       + num_late_analyses + "/"
					       + num_analyses);

			// Busy-wait until the processing thread releases
			// the instrument
			while( instruments.get( e.symbol ).get()
			       != InstrumentsTable.IDLE ) {
			    Thread.yield();
			}
		    }

		    queue.offer( e );
		}
	    }
	    else
		queue.offer( e );
	}

	// Terminate all threads
	for( int i=0; i < threads.length; ++i )
	    queue.offer( new Event() );

	for( int i=0; i < threads.length; ++i )
	    try {
		threads[i].join();
	    } catch( Exception exc ) {
		System.err.println( "Exception during join: " + exc );
	    }

	// This line must be printed on System.out
	float fraction = 100.0f * ( num_analyses - num_late_analyses );
	fraction /= num_analyses;
	System.out.println( "Percentage succesful analyses: "
			    + String.format( "%.2f", fraction ) );
    }

    // Do not modify this method
    boolean processEvent( Event e ) {
	// If a trade is made, then revalue all options on that trace.
	// Need to initalise number of options for every symbol.
	switch( e.type ) {
	    case Event.TERMINATE:
		return false;
	    case Event.TRADE:
		processTrade( e.symbol, e.bidPrice, e.askPrice );
		break;
	    case Event.ADDSYM:
		processAddSymbol( e.symbol );
		break;
	    case Event.RMVSYM:
		processRmvSymbol( e.symbol );
		break;
	}
	return true;
    }

    // The following method may be modified
    // Create an instrument in both the instruments and options tables
    void createSymbol( String symbol ) {
	if( instruments.get( symbol ) == null ) {
	    instruments.create( symbol );
	    options.createSymbol( symbol );
	}
    }

    // The following method may be modified
    // Create an option for an instrument
    void createOption( String symbol, double strike, double deadline,
		       double volatility, char type ) {
	// If instrument wat not seen before, create it now.
	if( instruments.get( symbol ) == null ) {
	    instruments.create( symbol );
	    options.createSymbol( symbol );
	}
	options.createOption( symbol, 0f, strike, deadline, volatility, type );
    }

    // The following method may be modified
    // Process a stock trade, i.e., recalculate the value of all options
    // on this stock.
    void processTrade( String symbol, double bidPrice, double askPrice ) {
	// If we cannot change the state from IDLE to BUSY, it must have been
	// set to LATE, so we return immediately.
	if( !instruments.update( symbol, InstrumentsTable.IDLE, InstrumentsTable.BUSY ) ) {
        return;
    }
	// System.out.println( "Instrument " + symbol + " acquired" );
	// Recalculate all options on this instrument
	long tm_start = System.nanoTime();
	int num_processed = 0;

	for( Option option : options.get( symbol ) ) {
	    // Abort if a new trade update have arrived
	    if( instruments.get( symbol ).get() == InstrumentsTable.LATE ) {
		// System.out.println("Got LATE for instrument " + symbol
		// + " , abort");
		// Stop processing and record we are idle.
		instruments.update( symbol, InstrumentsTable.LATE, InstrumentsTable.IDLE );
		return;
	    }
	    double base_price = option.getType() == 'C' ? askPrice : bidPrice;
	    double projected_value = estimate_value( option, base_price );
	    option.setValue( projected_value );
	    // System.out.println( "Option on " + symbol + ": " + projected_value );
	    ++num_processed;
	}
	long tm_delay = System.nanoTime() - tm_start;
	if( verbose )
	    System.out.println( "ProcessTrade: " + symbol + " " + num_processed
				+ "/" + options.get( symbol ).size()
				+ String.format( " in %.3f seconds",
						 (double)tm_delay * 1e-9 ) );

	// Record that options for this instrument have been updated
	instruments.get( symbol ).set( InstrumentsTable.IDLE );
	// System.out.println( "Instrument " + symbol + " released" );
    }

    // The following method may be modified
    void processAddSymbol( String symbol ) {
	instruments.create( symbol );
    }

    // The following method may be modified
    void processRmvSymbol( String symbol ) {
	instruments.remove( symbol );
    }

    // Do not modify this method
    double estimate_value( Option option, double price ) {
	return BinomialOptionPricing( price, option.getStrike(), 
		option.getDeadline(),
		option.getVolatility(),
		option.getType() );
    }

    // Do not modify this function!
    double BinomialOptionPricing( double Sx, double Xx, double Tx, double Vx, char type ) {
	double[] price = new double[NUM_STEPS+1];

	double dt = Tx / (double)NUM_STEPS;

	double vDt = Vx * Math.sqrt(dt);
	double rDt = RISKFREE * dt;

	double If = Math.exp(rDt);
	double Df = Math.exp(-rDt);

	double u = Math.exp(vDt);
	double d = Math.exp(-vDt);
	double pu = (If - d) / (u - d);
	double pd = 1.0f - pu;
	double puByDf = pu * Df;
	double pdByDf = pd * Df;

	for(int i = 0; i <= NUM_STEPS; i++) {
	    double dd = Sx * (double)Math.exp(vDt * (2.0 * i - NUM_STEPS)) - Xx;
	    if (type == 'P' )
		dd = -dd;
	    price[i] =  (dd > 0) ? dd : 0;
	}

	for(int i = NUM_STEPS; i > 0; i--)
	    for(int j = 0; j <= i - 1; j++)
		price[j] = puByDf * price[j + 1] + pdByDf * price[j];

	return price[0];
    }

    double NormalCDF(double zz)
	{
	    // cdf of 0 is 0.5
	    if (zz == 0)
	    {
		return 0.5;
	    }
	    double z = zz; //zz is input variable, use z for calculations
	    if (zz < 0)
		z = -zz; //change negative values to positive
	    // set constants
	    double p = 0.2316419;
	    double b1 = 0.31938153;
	    double b2 = -0.356563782;
	    double b3 = 1.781477937;
	    double b4 = -1.821255978;
	    double b5 = 1.330274428;
	    // CALCULATIONS
	    double f = 1 / Math.sqrt(2 * Math.PI);
	    double ff = Math.exp(-Math.pow(z, 2) / 2) * f;
	    double s1 = b1 / (1 + p * z);
	    double s2 = b2 / Math.pow((1 + p * z), 2);
	    double s3 = b3 / Math.pow((1 + p * z), 3);
	    double s4 = b4 / Math.pow((1 + p * z), 4);
	    double s5 = b5 / Math.pow((1 + p * z), 5);
	    // sz is the right-tail approximation
	    double sz = ff * (s1 + s2 + s3 + s4 + s5);

	    double rz = 0.0;
	    // cdf of negative input is right-tail of input's absolute value
	    if (zz < 0)
		rz = sz;
	    //cdf of positive input is one minus right-tail
	    if (zz > 0)
		rz = (1 - sz);
	    return rz;
	}
}
