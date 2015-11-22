package chaomai.ckdtree.snapshot2;

import chaomai.ckdtree.ICKDTreeMap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chaomai on 11/20/15.
 */

@SuppressWarnings({"unused"})
public class CKDTreeMap<V> implements ICKDTreeMap<V> {
  final InternalNode<V> root;
  private final int dimension;

  public CKDTreeMap(int dimension) {

    this.dimension = dimension;

    double[] key = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      key[i] = Double.POSITIVE_INFINITY;
    }

    this.root = new InternalNode<>(key, new Leaf<>(key), new Leaf<>(key), 0);
  }

  private boolean keyEqual(double[] k1, double[] k2) {
    return Arrays.equals(k1, k2);
  }

  private int keyCompare(double[] k1, double[] k2, int depth) {
    if (k1[0] == Double.POSITIVE_INFINITY && k2[0] == Double.POSITIVE_INFINITY) {
      return -1;
    }

    if (Arrays.equals(k1, k2)) {
      return 0;
    }

    int axis = depth % this.dimension;

    if (k1[axis] < k2[axis]) {
      return -1;
    } else if (k1[axis] == k2[axis]) {
      return 0;
    } else {
      return 1;
    }
  }

  private Object searchKey(double[] key) {
    InternalNode<V> gp = null;
    Update gpupdate = null;
    InternalNode<V> p = null;
    Update pupdate = null;
    Leaf<V> l;
    int depth = 0;

    Node<V> cur = this.root;

    while (cur instanceof InternalNode) {
      gp = p;
      gpupdate = pupdate;
      p = (InternalNode<V>) cur;
      pupdate = p.GET_UPDATE();
      depth += p.skippedDepth;

      if (keyCompare(key, cur.key, depth++) < 0) {
        cur = p.left;
      } else {
        cur = p.right;
      }
    }

    l = (Leaf<V>) cur;

    return new SearchRes<>(gp, gpupdate, p, pupdate, l, depth);
  }

  SearchRes<V> search(double[] key) {
    while (true) {
      Object res = searchKey(key);

      if (res == SearchRes.RESTART) {
        continue;
      } else {
        return (SearchRes<V>) res;
      }
    }
  }

  @Override
  public boolean add(double[] key, V value) {
    return false;
  }

  @Override
  public void clear() {

  }

  @Override
  public boolean contains(double[] key) {
    SearchRes sr = search(key);
    return keyEqual(sr.l.key, key);
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

  // todo: implement snapshot method mentioned in http://www.cs.utoronto.ca/~tabrown/ksts/StaticDictionary5.java
  @Override
  public CKDTreeMap<V> snapshot() {
    return null;
  }

  @Override
  public CKDTreeMap<V> readOnlySnapshot() {
    return null;
  }
}