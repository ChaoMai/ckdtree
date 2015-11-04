package chaomai.ckdtree;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by chaomai on 11/3/15.
 */
public class CKDTreeMapTest {
  double delta = 0.001;

  @Test
  public void testConstructor() throws Exception {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    InternalNode<Integer> root = ckd.readRoot();

    assertEquals(Double.NEGATIVE_INFINITY, root.left.key[0], delta);
    assertEquals(Double.POSITIVE_INFINITY, root.right.key[0], delta);
  }

  private void search1() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double key1[] = {Double.NEGATIVE_INFINITY};

    Object res = ckd.search(key1);

    assertTrue(ckd.contains(key1));

    double key2[] = {Double.POSITIVE_INFINITY};
    assertTrue(ckd.contains(key2));
  }

  @Test
  public void testSearchKey() throws Exception {
    search1();
  }


  @Test
  public void testSearch() throws Exception {

  }

  @Test
  public void testAdd() throws Exception {

  }
}