package chaomai.ckdtree.snapshot1;

import org.junit.Test;

/**
 * Created by chaomai on 11/1/15.
 */
public class StateTest {
  @Test
  public void testConstructor() {
    class B {
      int a;
      State state;

      @Override
      public String toString() {
        return a + " " + state;
      }
    }

    B b = new B();
    System.out.println(b);
  }
}
