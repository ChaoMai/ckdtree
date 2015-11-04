package chaomai.ckdtree;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by chaomai on 11/3/15.
 */
public class CKDTreeMapTest {
  double delta = 0.001;

  @Test
  public void testConstructor() throws Exception {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<Integer>(1);
    InternalNode<Integer> root = ckd.readRoot();

    assertEquals(Double.NEGATIVE_INFINITY, root.left.key[0], delta);
    assertEquals(Double.POSITIVE_INFINITY, root.right.key[0], delta);
  }

  @Test
  public void testSearchKey() throws Exception {

  }

  @Test
  public void testSearch() throws Exception {

  }

  @Test
  public void testAdd() throws Exception {

  }
}