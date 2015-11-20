package chaomai.ckdtree.snapshot1;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static org.junit.Assert.*;

/**
 * Created by chaomai on 11/1/15.
 */
public class MiscTest {
  double delta = 0.001;

  @Test
  public void testArray() {
    Object o;
    double[] arr = {1, 2, 3, 4, 5, 6};
    o = arr;
    assertEquals("class [D", o.getClass().toString());

    assertTrue(o.equals(arr));

    double[] arr2 = {1, 2, 3, 4, 5, 6};
    assertTrue(Arrays.equals(arr, arr2));
  }

  @Test
  public void testFinal() {
    class U {
      int b;

      U(int b) {
        this.b = b;
      }

      @Override
      public String toString() {
        return "U " + b;
      }
    }

    class CA {
      final int b;
      final U c;
      int a;

      CA(int a, int b, U c) {
        this.a = a;
        this.b = b;
        this.c = c;
      }

      CA renewed(int n) {
        return new CA(n, b, c);
      }

      CA deepRenewed(int n) {
        return new CA(n, b, new U(n));
      }

      @Override
      public String toString() {
        return "CA " + a + " " + b + " " + c;
      }
    }

    U u = new U(11);
    CA a = new CA(1, 1, u);
    CA na = a.renewed(2);

    assertEquals("CA 1 1 U 11", a.toString());
    assertEquals("CA 2 1 U 11", na.toString());

    CA na1 = na.renewed(3);
    u.b = 12;
    assertEquals("CA 3 1 U 12", na1.toString());

    CA na2 = na.deepRenewed(4);
    u.b = 13;
    assertEquals("CA 4 1 U 4", na2.toString());
  }

  @Test
  public void testTypeCast() {
    class V {
      int t;
    }

    class N {
      int a;

      N(int a) {
        this.a = a;
      }
    }

    class A<T> extends N {
      N n;
      T a;

      A(int a, T b) {
        super(a);
        this.a = b;
      }
    }

    class B<T> extends N {
      T a;
      int b;
      N n;

      B(int a, int b, T c) {
        super(a);
        this.b = b;
        this.a = c;
      }
    }

    V v = new V();
    v.t = 999;

    A<V> a = new A<V>(1, v);
    B<V> b = new B<V>(2, 3, v);
    a.n = b;

    N n = a;

    B nb = (B) a.n;
    B<V> nb1 = (B<V>) a.n;

    assertEquals(3, nb.b);
    assertEquals(3, nb1.b);

    class C extends N {
      int a;

      C(int a) {
        super(a);
      }

      int m(A a) {
        B nb = (B) a.n;
        return nb.b;
      }
    }

    C c = new C(9);
    assertEquals(3, c.m(a));

    class D<T> extends N {
      int a;

      D(int a) {
        super(a);
      }

      int m(A<T> a) {
        B<T> nb = (B<T>) a.n;
        return nb.b;
      }
    }

    D<V> d = new D<V>(9);
    assertEquals(3, d.m(a));
  }

  @Test
  public void testDoubleMax() {
    //assertEquals(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 0.0001);
    assertEquals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY - 1, delta);
  }

  @Test
  public void testArrayAssignment() {
    double[] key = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
    double[] leftKey = key.clone();
    leftKey[0] = Double.NEGATIVE_INFINITY;
    assertFalse(Arrays.equals(key, leftKey));
  }

  @Test
  public void testObjectFiled() {
    class A {
      int a;
      int b;

      A(int a, int b) {
        this.a = a;
        this.b = b;
      }
    }

    class B {
      int c;

      B(int c) {
        this.c = c;
      }
    }

    class C {
      volatile A a;
      volatile B b;

      AtomicReferenceFieldUpdater<C, A> aUpdater =
          AtomicReferenceFieldUpdater.newUpdater(C.class, A.class, "a");

      C(int a, int b, int c) {
        this.a = new A(a, b);
        this.b = new B(c);
      }

      C(A a, B b) {
        this.a = a;
        this.b = b;
      }
    }

    A a = new A(1, 2);
    B b = new B(3);
    C c = new C(a, b);
    Assert.assertEquals(1, c.a.a);
    Assert.assertEquals(2, c.a.b);
    Assert.assertEquals(3, c.b.c);

    a.a = 100;

    Assert.assertEquals(100, c.a.a);

    A na = new A(8, 9);
    c.aUpdater.compareAndSet(c, a, na);

    Assert.assertEquals(8, c.a.a);
    Assert.assertEquals(9, c.a.b);
  }

  @Test
  public void testFinal2() {
    class A {
      final int a;
      final int b;

      A(int a, int b) {
        this.a = a;
        this.b = b;
      }
    }

    A a = new A(1, 2);

    Assert.assertEquals(a.a, 1);
    //    a.a = 3;
  }
}