package com.yanchen.exercise.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yuyanchen on 16/11/4.
 */
public class Counter {
    private AtomicInteger atomicInteger = new AtomicInteger();
    private int i = 0;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        final Counter counter = new Counter();
        List<Thread> ts = new ArrayList<>(600);
        for (int j = 0; j < 100; j++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        counter.count();
                        counter.safeCount();
                    }
                }
            });
            ts.add(thread);
        }
        for (Thread t : ts) {
            t.start();
        }

        for (Thread t : ts) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(counter.atomicInteger.get());
        System.out.println(counter.i);
        System.out.println(System.currentTimeMillis() - start);
    }

    private void safeCount() {
        while (true) {
            int i = atomicInteger.get();
            boolean suc = atomicInteger.compareAndSet(i, ++i);
            if(suc) {
                break;
            }
        }
    }

    private void count() {
        i++;
    }
}
