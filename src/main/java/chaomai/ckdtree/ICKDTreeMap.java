package chaomai.ckdtree;

import chaomai.ckdtree.snapshot1.CKDTreeMap;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by chaomai on 11/20/15.
 */
@SuppressWarnings({"unused"})
public interface ICKDTreeMap<V> extends Iterable<Map.Entry<double[], V>> {
  boolean add(double[] key, V value);

  void clear();

  boolean contains(double[] key);

  @Override
  Iterator<Map.Entry<double[], V>> iterator();

  V get(Object key);

  boolean remove(double[] key);

  int size();

  CKDTreeMap<V> snapshot();

  CKDTreeMap<V> readOnlySnapshot();

  @Override
  String toString();
}
