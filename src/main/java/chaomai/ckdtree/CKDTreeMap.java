package chaomai.ckdtree;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by chaomai on 11/1/15.
 */
//public class CKDTreeMap<V> extends AbstractSet<V> {
public class CKDTreeMap<V> {
  private InternalNode<V> root;
  private final boolean readOnly;

  CKDTreeMap(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  CKDTreeMap(final InternalNode<V> r, final boolean readOnly) {
    this(readOnly);
    this.root = r;
  }

  CKDTreeMap() {
    this(false);
  }

  InternalNode<V> readRoot() {
    return null;
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
//      // if children are InternalNode, then check their generation.
//      Node left = ((InternalNode) cur).left;
//
//      // only perform GCAS on InternalNode
//      if (left instanceof InternalNode) {
//        if (((InternalNode) left).gen != startGen) {
//          if (((InternalNode) cur).GCAS((InternalNode) left, ((InternalNode) left).renewed(startGen), this)) {
//            //retry on cur
//            continue;
//          } else {
//            return SearchRes.RESTART;
//          }
//        }
//      }

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
                this)) {
              // retry on cur
              continue;
            } else {
              return SearchRes.RESTART;
            }
          }
        }

        cur = p.left;
      } else {
        // if left child are InternalNode, then check their generation.

        cur = p.right;
      }
    }

    // todo: check type?
    l = (Leaf) cur;

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

  public boolean add(Object key, V value) {
    return false;
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