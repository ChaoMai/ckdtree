package chaomai.ckdtree.snapshot2;

import chaomai.ckdtree.ICKDTreeMap;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by chaomai on 11/20/15.
 */
@SuppressWarnings({"unused"})
public class CKDTreeMap<V> implements ICKDTreeMap<V> {
  @Override
  public boolean add(double[] key, V value) {
    return false;
  }

  @Override
  public void clear() {

  }

  @Override
  public boolean contains(double[] key) {
    return false;
  }

  @Override
  public Iterator<Map.Entry<double[], V>> iterator() {
    return null;
  }

  @Override
  public V get(Object key) {
    return null;
  }

  @Override
  public boolean remove(double[] key) {
    return false;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public CKDTreeMap<V> snapshot() {
    return null;
  }

  @Override
  public CKDTreeMap<V> readOnlySnapshot() {
    return null;
  }
}