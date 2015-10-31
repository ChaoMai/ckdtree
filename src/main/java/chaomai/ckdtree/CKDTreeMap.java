package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
public class CKDTreeMap<V> implements IKDTreeMap<V> {
  @Override
  public boolean insert(double[] key, V data) {
    return false;
  }

  @Override
  public V get(double[] key) {
    return null;
  }

  @Override
  public boolean set(double[] key, V data) {
    return false;
  }

  @Override
  public boolean delete(double[] key) {
    return false;
  }

  @Override
  public int getNodeCount() {
    return 0;
  }

  @Override
  public NearestNeighborList nearest(double[] target, int n) {
    return null;
  }

  @Override
  public NearestNeighborList nearest(double[] target) {
    return null;
  }
}
