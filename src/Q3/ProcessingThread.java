package Q3;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

// Do not modify this class, except for the queueing
class ProcessingThread extends Thread {
    final Valuation val;
    final PriorityBlockingQueue<Event> queue;

    ProcessingThread( Valuation val, PriorityBlockingQueue<Event> queue ) {
        this.val = val;
        this.queue = queue;
    }

    public void run() {
        while(true){
            try {
                Event e = queue.take();
                System.out.println("Thread " + this.getId() + " took " + e + "  out of the queue");
                if( val.processEvent( e ) == false )
                    break;
            } catch( InterruptedException exc ) {
                System.err.println( "Exception during join: " + exc );
            }
        }
    }
}
