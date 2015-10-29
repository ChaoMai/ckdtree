package chaomai.ckdtree;

import java.util.List;

public interface KDTreeMapInterface<TData> {
  boolean insert(double[] key, TData data);

  TData get(double[] key);

  boolean set(double[] key, TData data);

  boolean delete(double[] key);

  int getNodeCount();

  List<NearestResult> nearest(double[] target, int n);

  NearestResult nearest(double[] target);
}
