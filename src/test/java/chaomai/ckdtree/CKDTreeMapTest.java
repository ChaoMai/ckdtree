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

  private void searchDummy() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[] k1 = {Double.POSITIVE_INFINITY};

    Object res1 = ckd.search(k1);

    Assert.assertTrue(ckd.contains(k1));
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

    System.out.println(ckd.toString());
  }

  private void addMultipleKeys() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(3);
    int samples = 5;
    int dimension = 3;
    double[][] array = Utilties.generateRandomArrays(samples, dimension);

    for (int i = 0; i < samples; ++i) {
      ckd.add(array[i], i);
    }

    //    System.out.println(ckd.toString());

    //    for (int i = 0; i < samples; ++i) {
    //      double[] k = array[i];
    //      Assert.assertTrue(ckd.contains(k));
    //
    //      Object res = ckd.search(k);
    //      Assert.assertNotEquals(null, res);
    //
    //      SearchRes<Integer> r = (SearchRes<Integer>) res;
    //      Assert.assertArrayEquals(k, r.l.key, delta);
    //    }
  }

  @Test
  public void testAdd() throws Exception {
    addOneKey();
    addMultipleKeys();
  }
}