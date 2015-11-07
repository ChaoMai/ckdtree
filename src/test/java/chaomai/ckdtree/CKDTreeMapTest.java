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

  private void addOneDimensionKeys(int samples) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[][] k = Utilties.generateRandomArrays(5000, 1);

    k[1][0] = k[2][0];

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
    // at some point, the key of newInternal would equal to its parent's key.
    double[][] k = {{5.305068244152987, 5.084449022627336, 4.155634301794545},
                    {4.658607614580709, 1.112285238547236, 7.6704533893483875},
                    {3.135000004662376, 4.737773994443383, 3.8336349759006993},
                    {1.0351259060545581, 4.21039722994082, 2.4693577126537414},
                    {5.877263378165557, 2.2656014079486053, 0.358466039752825}};

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

  private void addMultipleDimensionKeys2(int samples, int dimension) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k = Utilties.generateRandomArrays(samples, dimension);

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

  private void testSingleThreadAdd() {
    System.out.println("basic:");
    System.out.println("add One Key");
    addOneKey();

    for (int i = 1; i < 10; ++i) {
      System.out.println("round" + i);
      System.out.println("add One Dimension Keys");
      addOneDimensionKeys(i * 1000);
      addMultipleDimensionKeys2(i * 1000, i);
      System.out.println(String.format("add Multiple (%d) Dimension (%d) Keys", i * 1000, i));
    }

    System.out.println("add Special Key Sequences");
    addMultipleDimensionKeys();
    addMultipleDimensionKeys1();
  }

  @Test
  public void testAdd() throws Exception {
    testSingleThreadAdd();
  }
}