package chaomai.ckdtree;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by chaomai on 11/1/15.
 */
//public class CKDTreeMap<V> extends AbstractSet<V> {
public class CKDTreeMap<V> {
  private InternalNode<V> root;
  private final int dimension;
  private final boolean readOnly;

  CKDTreeMap(final boolean readOnly, final int dimension) {
    this.readOnly = readOnly;
    this.dimension = dimension;

    double[] key = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      key[i] = Double.POSITIVE_INFINITY;
    }

    double[] leftKey = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      leftKey[i] = Double.NEGATIVE_INFINITY;
    }

    root = new InternalNode<V>(key, new Leaf<V>(leftKey), new Leaf<V>(key), new Gen());
  }

  public CKDTreeMap(final int dimension) {
    this(false, dimension);
  }

  boolean isReadOnly() {
    return readOnly;
  }

  boolean nonReadOnly() {
    return !readOnly;
  }

  InternalNode<V> readRoot() {
    return root;
  }

  boolean keyEqual(double[] k1, double[] k2) {
    return Arrays.equals(k1, k2);
  }

  int keyCompare(double[] k1, double[] k2, int axis) {
    if (Arrays.equals(k1, k2)) {
      return 0;
    }

    if (k1[axis] < k2[axis]) {
      return -1;
    } else if (k1[axis] == k2[axis]) {
      return 0;
    } else {
      return 1;
    }
  }

  Object searchKey(double[] key, Gen startGen) {
    InternalNode<V> gp = null;
    Update gpupdate = null;
    InternalNode<V> p = null;
    Update pupdate = null;
    Leaf<V> l;
    int depth = 0;

    Node cur = root;

    while (cur instanceof InternalNode) {
      // continue searching
      gp = p;
      gpupdate = pupdate;
      p = (InternalNode<V>) cur;
      pupdate = p.update;
      depth += p.skippedDepth;

      if (keyCompare(key, cur.key, depth++) < 0) {
        // if left child are InternalNode, then check their generation.
        Node left = ((InternalNode<V>) cur).left;

        // only perform GCAS on InternalNode
        if (left instanceof InternalNode) {
          if (((InternalNode<V>) left).gen != startGen) {
            // do GCAS, change the left into a new with new gen.
            if (((InternalNode<V>) cur).GCAS(
                (InternalNode<V>) left,
                ((InternalNode<V>) left).renewed(startGen),
                this,
                Direction.LEFT)) {
              // retry on cur
              continue;
            } else {
              return SearchRes.RESTART;
            }
          }
        }

        cur = p.left;
      } else {
        // if right child are InternalNode, then check their generation.
        Node right = ((InternalNode<V>) cur).left;

        if (right instanceof InternalNode) {
          if (((InternalNode) right).gen != startGen) {
            if (((InternalNode<V>) cur).GCAS(
                (InternalNode<V>) right,
                ((InternalNode<V>) right).renewed(startGen),
                this,
                Direction.RIGHT)) {
              continue;
            } else {
              return SearchRes.RESTART;
            }
          }
        }

        cur = p.right;
      }
    }

    // todo: check type?
    l = (Leaf<V>) cur;

    return new SearchRes<V>(gp, gpupdate, p, pupdate, l, depth);
  }

  SearchRes<V> search(double[] key) {
    while (true) {
      Object res = searchKey(key, root.gen);

      if (res == SearchRes.RESTART) {
        continue;
      } else {
        // todo: check type?
        return (SearchRes<V>) res;
      }
    }
  }

  boolean insert(double[] key, V vale) {
    while (true) {
      SearchRes<V> r = search(key);

      if (keyEqual(r.l.key, key)) {
        return false;
      }

      if (r.pupdate.state != State.CLEAN) {
        // todo: change into helping it
        continue;
      }
    }
  }

  public boolean add(double[] key, V value) {
    return insert(key, value);
  }

  public void clear() {

  }

  public boolean contains(double[] key) {
    SearchRes sr = search(key);
    return keyEqual(sr.l.key, key);
  }

  public Iterator<V> iterator() {
    return null;
  }

  public V get(Object key) {
    return null;
  }

  public CKDTreeMap<V> readOnlySnapshot() {
    return null;
  }

  public boolean remove(double[] key) {
    return false;
  }

  public int size() {
    return 0;
  }

  public CKDTreeMap<V> snapshot() {
    return null;
  }
}