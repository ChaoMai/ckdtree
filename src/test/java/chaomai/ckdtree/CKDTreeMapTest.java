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

    Assert.assertEquals(Double.POSITIVE_INFINITY, root.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.left.key[0], delta);
    Assert.assertEquals(null, root.right);
  }

  private void searchDummy() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[] k = {Double.POSITIVE_INFINITY};

    Object res1 = ckd.search(k);

    Assert.assertTrue(ckd.contains(k));
  }

  @Test
  public void testSearch() throws Exception {
    searchDummy();
  }

  private void addOneKey() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[] k1 = {1};
    ckd.add(k1, 1);

    Assert.assertTrue(ckd.contains(k1));

    Object res = ckd.search(k1);
    Assert.assertNotEquals(null, res);

    SearchRes<Integer> r = (SearchRes<Integer>) res;
    Assert.assertArrayEquals(k1, r.l.key, delta);
  }

  private void addSomeKeys() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[][] k = Utilties.generateRandomArrays(5000, 1);

    k[100][0] = k[101][0];

    for (int i = 0; i < k.length; ++i) {
      ckd.add(k[i], i);
    }

    for (int i = 0; i < k.length; ++i) {
      double[] key = k[i];
      Assert.assertTrue(ckd.contains(key));

      Object res = ckd.search(key);
      Assert.assertNotEquals(null, res);

      SearchRes<Integer> r = (SearchRes<Integer>) res;
      Assert.assertArrayEquals(key, r.l.key, delta);
    }
  }

  private void addMultipleDimensionKeys() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(3);
    double[][] k =
        {{1, 2, 3}, {2, 3, 4}, {3, 4, 5}, {4, 5, 6}, {5, 6, 7}, {6, 7, 8}, {7, 8, 9}, {9, 10, 11},
         {11, 12, 13}};

    for (int i = 0; i < k.length; ++i) {
      ckd.add(k[i], i);
    }

    for (double[] key : k) {
      Assert.assertTrue(ckd.contains(key));

      Object res = ckd.search(key);
      Assert.assertNotEquals(null, res);

      SearchRes<Integer> r = (SearchRes<Integer>) res;
      Assert.assertArrayEquals(key, r.l.key, delta);
    }
  }

  private void addMultipleDimensionKeys1() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(3);
    double[][] k = Utilties.generateRandomArrays(50, 3);

    for (int i = 0; i < k.length; ++i) {
      ckd.add(k[i], i);
    }

    for (double[] key : k) {
      Assert.assertTrue(ckd.contains(key));

      Object res = ckd.search(key);
      Assert.assertNotEquals(null, res);

      SearchRes<Integer> r = (SearchRes<Integer>) res;
      Assert.assertArrayEquals(key, r.l.key, delta);
    }
  }

  @Test
  public void testAdd() throws Exception {
    addOneKey();
    addSomeKeys();
    addMultipleDimensionKeys();
    addMultipleDimensionKeys1();
  }
}