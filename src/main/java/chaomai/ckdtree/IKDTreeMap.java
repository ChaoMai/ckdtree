package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
interface IKDTreeMap<V> {

  boolean insert(double[] key, V data);

  V get(double[] key);

  boolean set(double[] key, V data);

  boolean delete(double[] key);

  int getNodeCount();

  NearestNeighborList nearest(double[] target, int n);

  NearestNeighborList nearest(double[] target);
}
