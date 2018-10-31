package nachos.threads;

import java.util.Comparator;
import java.util.PriorityQueue;
import nachos.machine.*;


/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm 
{

    //create waitQueue of WaitingThreads, ordered through TimeComparator
    PriorityQueue<WaitingThread> waitQueue = new PriorityQueue<WaitingThread>(10, new TimeCompare()); //initial capacity, order WaitingComparator

    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() 
    {
	
        Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() 
    {
	
        Machine.interrupt().disable(); //disable

        //if waitingQueue is empty, and current time is greater than or equal to the first WaitingThreads, wakeUp time,
        while(!waitQueue.isEmpty() && Machine.timer().getTime() >= waitQueue.peek().wakeUp)
        {
            waitQueue.poll().thread.ready(); //pop head
        }

        KThread.currentThread().yield();

        Machine.interrupt().enable(); //enable

    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) 
    {

        Machine.interrupt().disable(); //disable interrupts

	    long wakeTime = Machine.timer().getTime() + x; //calculate wakeTime

        //pass through wakeTime and current thread as instance variables for a
        WaitingThread a = new WaitingThread(wakeTime, KThread.currentThread());

        waitQueue.add(a); //add a to the waitQueue 

        KThread.currentThread().sleep(); //sleep current thread

        Machine.interrupt().enable(); //enable interrupts
    }

    public class WaitingThread //PriorityQueue of waiting threads
    {
        long wakeUp; //time to wake up;
        KThread thread; //thread associated with

        WaitingThread(long wakeUp, KThread thread) //initialize with these variables
        {
            this.wakeUp = wakeUp;
            this.thread = thread;
        }

    }

    public class TimeCompare implements Comparator<WaitingThread> //for comparing wait times
    {
        @Override
        public int compare(WaitingThread a, WaitingThread b)
        {
            if(a.wakeUp < b.wakeUp)
            {
                return -1;
            }
            else if (a.wakeUp == b.wakeUp)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }
}
