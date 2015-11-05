package chaomai.ckdtree;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chaomai on 11/3/15.
 */
public class CKDTreeMapTest {
  double delta = 0.001;

  @Test
  public void testConstructor() throws Exception {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    InternalNode<Integer> root = ckd.readRoot();

    Assert.assertEquals(Double.NEGATIVE_INFINITY, root.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.left.key[0], delta);
    Assert.assertEquals(null, root.right);
  }

  private void search1() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double k1[] = {Double.POSITIVE_INFINITY};

    Object res1 = ckd.search(k1);

    Assert.assertTrue(ckd.contains(k1));
  }

  @Test
  public void testSearch() throws Exception {
    search1();
  }

  private void add1() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double k1[] = {1};
    ckd.add(k1, 1);
    Object res = ckd.search(k1);
    Assert.assertNotEquals(null, res);

    SearchRes<Integer> r = (SearchRes<Integer>) res;
    Assert.assertEquals(k1, r.l.key);
  }

  @Test
  public void testAdd() throws Exception {
    add1();
  }
}