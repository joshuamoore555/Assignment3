package Q1;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Workshop {
    static Semaphore elfSemaphore = new Semaphore(0);
    static Semaphore reindeerSemaphore = new Semaphore(1);
    static Semaphore santaSemaphore = new Semaphore(1);

    private static AtomicInteger santaCount = new AtomicInteger(0);
    private static AtomicInteger elfCount = new AtomicInteger(0);
    private static AtomicInteger reindeerCount = new AtomicInteger(0);

    public static void main(String args[]) {
        new Santa().start();

        for(int i = 0; i < 40; i++){
            new Elf("Elf " + i).start();
        }
        new Reindeer("Prancer").start();
        new Reindeer("Dancer").start();

    }


    public static class Santa extends Thread{
        @Override
        public void run() {
            try {
                while (true) {
                    if(elfCount.get() == 0 && reindeerCount.get() == 0){
                        System.out.println("There are "+ elfCount.get() + " elves and " + reindeerCount.get() + " reindeer so Santa entered");
                        santaSemaphore.acquire();
                        santaCount.getAndIncrement();


                        System.out.println("Santa left");
                        santaSemaphore.release();
                        santaCount.getAndDecrement();
                    }
                    Thread.sleep(6000);
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
                    elfSemaphore.release(1);
                    elfCount.getAndIncrement();
                    System.out.println(name + " entered                                                                                                                              " + elfCount.get() + "    "+elfSemaphore.availablePermits());



                    System.out.println(name + " left                                                                                                                                 " + elfCount.get()+ "    "+elfSemaphore.availablePermits());
                    elfSemaphore.acquire(1);
                    elfCount.getAndDecrement();
                    Thread.sleep(2000);
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
                    if(santaCount.get() == 0 && elfCount.get() >= 4){
                        reindeerSemaphore.acquire(1);
                        elfSemaphore.acquire(2);
                        System.out.println("                                                                                  "+name + " the Reindeer entered." + " He has " + elfCount.get() + " elves minding him. " + elfSemaphore.availablePermits());
                        reindeerCount.getAndIncrement();

                        reindeerCount.getAndDecrement();
                        System.out.println("Reindeer left");
                        reindeerSemaphore.release(1);
                        elfSemaphore.release(2);

                        Thread.sleep(6000);
                    }
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
