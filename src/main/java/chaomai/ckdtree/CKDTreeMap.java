package chaomai.ckdtree;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chaomai on 11/1/15.
 */

@SuppressWarnings({"unused"})
//public class CKDTreeMap<V> extends AbstractSet<V> {
public class CKDTreeMap<V> {
  private InternalNode<V> root;
  private final int dimension;
  private final boolean readOnly;
  private final AtomicInteger size = new AtomicInteger();

  CKDTreeMap(final boolean readOnly, final int dimension) {
    this.readOnly = readOnly;
    this.dimension = dimension;

    double[] key = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      key[i] = Double.POSITIVE_INFINITY;
    }

    double[] rootKey = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      rootKey[i] = Double.NEGATIVE_INFINITY;
    }

    root = new InternalNode<>(rootKey, new Leaf<>(key), null, new Gen());
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

  int keyCompare(double[] k1, double[] k2, int depth) {
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

  private Object searchKey(double[] key, Gen startGen) {
    InternalNode<V> gp = null;
    Update gpupdate = null;
    InternalNode<V> p = root;
    Update pupdate = root.GET_UPDATE();
    Leaf<V> l;
    int depth = 0;

    Node<V> cur = root.left;

    while (cur instanceof InternalNode) {
      // continue searching
      gp = p;
      gpupdate = pupdate;
      p = (InternalNode<V>) cur;
      pupdate = p.GET_UPDATE();
      depth += p.skippedDepth;

      if (keyCompare(key, cur.key, depth++) <= 0) {
        // if left child are InternalNode, then check their generation.
        Node<V> left = cur.left;

        // only perform GCAS on InternalNode
        if (left instanceof InternalNode) {
          if (left.gen != startGen) {
            // do GCAS, change the left into a new with new gen.
            if (cur.GCAS(left, ((InternalNode<V>) left).renewed(startGen), this, Direction.LEFT)) {
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
        Node<V> right = ((InternalNode<V>) cur).right;

        if (right instanceof InternalNode) {
          if (((InternalNode) right).gen != startGen) {
            if (cur
                .GCAS(right, ((InternalNode<V>) right).renewed(startGen), this, Direction.RIGHT)) {
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

    return new SearchRes<>(gp, gpupdate, p, pupdate, l, depth);
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

  private InternalNode<V> createSubTree(double[] k, V v, Leaf<V> l, int depth) {
    //    int skip = 0;
    //    don't find much help of this.
    //    int compareResult;
    //    while ((compareResult = keyCompare(k, l.key, depth++)) == 0) {
    //      ++skip;
    //    }

    Leaf<V> left;
    Leaf<V> right;
    double[] maxKey;

    if (keyCompare(k, l.key, depth) <= 0) {
      maxKey = l.key;
      left = new Leaf<>(k, v);
      right = new Leaf<>(l.key, l.value);
    } else {
      maxKey = k;
      left = new Leaf<>(l.key, l.value);
      right = new Leaf<>(k, v);
    }

    return new InternalNode<>(maxKey, left, right, root.gen);
  }

  private void help(Update update) {
    switch (update.state) {
      case IFLAG: {
        helpInsert(update);
        break;
      }
      case DFLAG: {
        break;
      }
      case MARK1: {
        break;
      }
      case MARK2: {
        break;
      }
    }
  }

  private void helpInsert(Update update) {
    InsertInfo<V> info = (InsertInfo<V>) update.info;
    Node<V> parent = (Node<V>) info.p;
    if (keyCompare(info.l.key, info.newInternal.key, update.depth) <= 0) {
      parent.GCAS(info.l, (Node<V>) info.newInternal, this, Direction.LEFT);
    } else {
      parent.GCAS(info.l, (Node<V>) info.newInternal, this, Direction.RIGHT);
    }
  }

  boolean insert(double[] key, V value) {
    while (true) {
      SearchRes<V> r = search(key);

      if (keyEqual(r.l.key, key)) {
        return false;
      }

      if (r.pupdate.state != State.CLEAN) {
        // todo: change into helping it
        return false;
      } else {
        InternalNode<V> newInternal = createSubTree(key, value, r.l, r.leafDepth);

        // IFlag
        InsertInfo<V> op = new InsertInfo<>(r.p, newInternal, r.l);
        Update nu = new Update(State.IFLAG, op, r.leafDepth);

        if (r.p.CAS_UPDATE(r.pupdate, nu)) {
          helpInsert(nu);
          return true;
        } else {
          Update update = r.p.GET_UPDATE();
          help(update);
        }
      }
    }
  }

  public boolean add(double[] key, V value) {
    while (true) {
      if (!insert(key, value)) {
        continue;
      } else {
        return true;
      }
    }
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
    return this.size.get();
  }

  public CKDTreeMap<V> snapshot() {
    return null;
  }
}