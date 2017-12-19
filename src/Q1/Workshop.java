package Q1;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Workshop {
    private static AtomicInteger numberOfElves = new AtomicInteger(4);
    static Semaphore elvesOutside = new Semaphore(numberOfElves.get(), true);
    static Semaphore elvesInside = new Semaphore(0, true);

    static Semaphore santaSemaphore = new Semaphore(1, true);
    static Semaphore reindeerSemaphore = new Semaphore(1, true);
    static Semaphore mutex = new Semaphore(1, true);

    public static void main(String args[]) {
        for(int i = 0; i < numberOfElves.get(); i++){
            new Elf("Elf " + i).start();
        }
        new Santa().start();
        new Reindeer("Prancer").start();
        new Reindeer("Dancer").start();
    }

    public static class Santa extends Thread{
        @Override
        public void run() {
            try {
                while (true) {
                    mutex.acquire(1);
                    santaSemaphore.acquire(1);
                    System.out.println("Santa Waiting..."  + elvesInside.availablePermits()+ "  " + elvesOutside.availablePermits());
                    reindeerSemaphore.acquire(1); //wait for reindeer to leave
                    elvesOutside.acquire(numberOfElves.get()); //wait for elves to be outside
                    mutex.release(1);

                    System.out.println("Santa Entered"  + elvesInside.availablePermits()+ "  " + elvesOutside.availablePermits());
                    elvesOutside.release(numberOfElves.get()); //allow elves to enter
                    reindeerSemaphore.release(1); //allow reindeer to attempt to enter


                    santaSemaphore.release(1);//santa leaves
                    System.out.println("Santa left"  + elvesInside.availablePermits()+ "  " + elvesOutside.availablePermits());
                    Thread.sleep(3000);


                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Elf extends Thread {
        String name;

        public Elf(String name){
            this.name = name;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    elvesOutside.acquire(1);
                    elvesInside.release(1);
                    System.out.println(name + " entered " + elvesInside.availablePermits()+ "  " + elvesOutside.availablePermits());


                    elvesInside.acquire(1);
                    elvesOutside.release(1);
                    System.out.println(name + " left " + elvesInside.availablePermits()+ "  " + elvesOutside.availablePermits());

                    Thread.sleep(3000);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Reindeer extends Thread {
        String name;

        public Reindeer(String name){
            this.name = name;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    //attempt to enter
                    mutex.acquire(1);
                    reindeerSemaphore.acquire(1);
                    System.out.println(name + " Waiting..."  + elvesInside.availablePermits()+ "  " + elvesOutside.availablePermits());
                    santaSemaphore.acquire(1); //wait for santa to leave
                    System.out.println("Santa isn't there!"  + elvesInside.availablePermits()+ "  " + elvesOutside.availablePermits());
                    elvesInside.acquire(4); //wait for 4 elves to enter
                    System.out.println("There are four elves! " + elvesInside.availablePermits() + "  " + elvesOutside.availablePermits());
                    mutex.release(1);
                    elvesInside.release(2); //2 elves can leave


                    System.out.println(name + " the Reindeer entered. " + elvesInside.availablePermits()+ "  " + elvesOutside.availablePermits());
                    santaSemaphore.release(1); //santa can enter

                    reindeerSemaphore.release(1);
                    elvesInside.release(2);
                    System.out.println(name + " the Reindeer left." + elvesInside.availablePermits()+ "  " + elvesOutside.availablePermits());

                    Thread.sleep(3000);

                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
