import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-6
 * Time: 下午9:04
 */
public class ThreadTest {
	SubThreadTest thread = new SubThreadTest(this);

	public void foo() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
//		ThreadTest tt = new ThreadTest();
		ArrayList<String> al = new ArrayList<String>();
		al.add("123");
		System.out.println(al);
		al.remove("321");
		System.out.println(al);
		al.add("123");
		System.out.println(al);
		al.remove("123");
		System.out.println(al);
	}
}

class SubThreadTest extends Thread {
	ThreadTest tt = null;

	public SubThreadTest(ThreadTest tt) {
		System.out.println("Thread Start");
		tt.foo();
		System.out.println("Thread End");
	}
}
