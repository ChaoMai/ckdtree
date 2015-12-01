package chaomai.ckdtree.snapshot3;

import chaomai.ckdtree.ICKDTreeMap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chaomai on 12/2/15.
 */
@SuppressWarnings({"unused"})
public class CKDTreeMap<V> implements ICKDTreeMap<V> {
  final Node<V> root;
  private final int dimension;
  private final AtomicInteger size = new AtomicInteger();

  private CKDTreeMap(Node<V> root, int dimension) {
    this.root = root;
    this.dimension = dimension;
  }

  public CKDTreeMap(int dimension) {

    this.dimension = dimension;

    double[] key = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      key[i] = Double.POSITIVE_INFINITY;
    }

    this.root = new Node<>(key, 0, new Node<>(key, null), new Node<>(key, null));
  }

  private boolean keyEqual(double[] k1, double[] k2) {
    return Arrays.equals(k1, k2);
  }

  private int keyCompare(double[] k1, double[] k2, int depth) {
    if (k1[0] == Double.POSITIVE_INFINITY && k2[0] == Double.POSITIVE_INFINITY) {
      return -1;
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

  SearchRes<V> search(double[] key) {
    Node<V> gp = null;
    Info gpinfo = null;
    Node<V> p = null;
    Info pinfo = null;
    Node<V> l = null;

    int depth = 0;
    Node<V> cur = this.root;

    while (cur.left != null) {
      gp = p;
      p = cur;
      depth += p.skippedDepth;

      if (keyCompare(key, cur.key, depth++) < 0) {
        cur = p.left;
      } else {
        cur = p.right;
      }
    }

    if (gp != null) {
      gpinfo = gp.info;
    }

    // p is doomed to be set.
    pinfo = p.info;
    l = cur;

    return new SearchRes<>(gp, gpinfo, p, pinfo, l, depth);
  }

  private void help(Info info) {
    if (info.getClass() == InsertInfo.class) {
      helpInsert((InsertInfo<V>) info);
    }
  }

  private void helpInsert(InsertInfo<V> info) {
    if (info.l == info.p.left) {
      // ichild
      if (info.p.CAS_LEFT(info.l, info.newInternal)) {
        this.size.getAndIncrement();
      }
    } else {
      // ichild
      if (info.p.CAS_RIGHT(info.l, info.newInternal)) {
        this.size.getAndIncrement();
      }
    }

    // unflag
    info.p.CAS_INFO(info, new Clean());
  }

  private Node<V> createSubTree(double[] k, V v, Node<V> l, int depth) {
    int skip = 0;
    int compareResult;

    while ((compareResult = keyCompare(k, l.key, depth++)) == 0) {
      ++skip;
    }

    Node<V> left;
    Node<V> right;
    double[] maxKey;

    if (compareResult < 0) {
      maxKey = l.key;
      left = new Node<>(k, v);
      right = new Node<>(l.key, l.value);
    } else {
      maxKey = k;
      left = new Node<>(l.key, l.value);
      right = new Node<>(k, v);
    }

    return new Node<>(maxKey, skip, left, right);
  }

  private boolean insert(double[] key, V value) {
    while (true) {
      SearchRes<V> sr = search(key);

      if (sr.l != sr.p.left && sr.l != sr.p.right) {
        continue;
      }

      if (keyEqual(sr.l.key, key)) {
        return false;
      }

      if (sr.p.info != null && sr.p.info.getClass() != Clean.class) {
        help(sr.p.info);
        continue;
      }

      Node<V> newInternal = createSubTree(key, value, sr.l, sr.leafDepth);

      // iflag
      InsertInfo<V> info = new InsertInfo<>(sr.p, newInternal, sr.l);

      if (sr.p.CAS_INFO(sr.pinfo, info)) {
        helpInsert(info);
        return true;
      } else {
        help(sr.p.info);
      }
    }
  }

  @Override
  public boolean add(double[] key, V value) {
    return insert(key, value);
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
    return this.size.get();
  }

  @Override
  public CKDTreeMap<V> snapshot() {
    return null;
  }
}