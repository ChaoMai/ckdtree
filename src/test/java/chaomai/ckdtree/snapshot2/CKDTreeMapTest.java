package chaomai.ckdtree.snapshot2;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chaomai on 11/22/15.
 */
public class CKDTreeMapTest {
  double delta = 0.001;
  boolean isVerbose = true;

  @Test
  public void testConstructor() throws Exception {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    InternalNode root = ckd.root;

    Assert.assertEquals(Double.POSITIVE_INFINITY, root.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.left.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.right.key[0], delta);
  }

  private void searchDummy() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[] k = {Double.POSITIVE_INFINITY};

    SearchRes<Integer> res1 = ckd.search(k);

    Assert.assertTrue(ckd.contains(k));
  }

  @Test
  public void testSearch() throws Exception {
    searchDummy();
  }

  @Test
  public void deleteOnekey() throws Exception {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);

    double[] k1 = {1};

    if (isVerbose) {
      System.out.println("adding");
    }
    ckd.add(k1, 1);

    Assert.assertTrue(ckd.contains(k1));

    SearchRes<Integer> res = ckd.search(k1);
    Assert.assertNotEquals(null, res);

    Assert.assertArrayEquals(k1, res.l.key, delta);


    if (isVerbose) {
      System.out.println("deleting");
    }
    ckd.remove(k1);

    Assert.assertFalse(ckd.contains(k1));

    SearchRes<Integer> res1 = ckd.search(k1);
    InternalNode root = ckd.root;

    Assert.assertEquals(null, res1.gp);
    Assert.assertEquals(root, res1.p);
    Assert.assertEquals(root.left, res1.l);
  }
}