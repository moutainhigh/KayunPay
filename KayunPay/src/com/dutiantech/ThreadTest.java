package com.dutiantech;

import java.util.Date;

public class ThreadTest {
	public static void main(String[] args) {
//		MyRunnable myRunable = new MyRunnable();
//		System.out.println("start at:" + new Date().getTime());
//		myRunable.start();
//		System.out.println("end at:" + new Date().getTime());
		sTest t = new sTest();
		t.test("array", String.valueOf(new Date().getTime()));
	}
}

class sTest {
	public void test(String... str) {
		for (String string : str) {
			System.out.println(string);
		}
	}
}

class MyRunnable extends Thread {
	private boolean stop;

	@Override
	public void run() {
		for (int i = 0; i < 100 && !stop; i++) {
			System.out.println("test test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test  test test ");
			System.out.println(Thread.currentThread().getName() + " " + i);
		}
	}

	public void stopThread() {
		this.stop = true;
	}

}