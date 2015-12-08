package chaomai.ckdtree.snapshot1;

import chaomai.ckdtree.ICKDTreeMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chaomai on 12/2/15.
 */

@SuppressWarnings({"unused"})
public final class CKDTreeMap<V> implements ICKDTreeMap<V> {
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

  private SearchRes<V> search(double[] key) {
    Node<V> gp = null;
    Info gpinfo = null;
    Node<V> p = null;
    Info pinfo;
    Node<V> l;

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

  private void help(Info info) {
    if (info.getClass() == InsertInfo.class) {
      helpInsert((InsertInfo<V>) info);
    } else if (info.getClass() == DeleteInfo.class) {
      helpDelete((DeleteInfo<V>) info);
    } else if (info.getClass() == Mark1.class) {
      helpMarked1(((Mark1<V>) info).deleteInfo);
    } else if (info.getClass() == Mark2.class) {
      helpMarked2(((Mark2<V>) info).deleteInfo);
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

  private boolean insert(double[] key, V value) {
    while (true) {
      final SearchRes<V> sr = search(key);

      if (sr.l != sr.p.left && sr.l != sr.p.right) {
        continue;
      }

      if (keyEqual(sr.l.key, key)) {
        return false;
      }

      if (sr.pinfo != null && sr.pinfo.getClass() != Clean.class) {
        help(sr.pinfo);
        continue;
      }

      final Node<V> newInternal = createSubTree(key, value, sr.l, sr.leafDepth);

      // iflag
      final InsertInfo<V> info = new InsertInfo<>(sr.p, newInternal, sr.l);

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
    final SearchRes sr = search(key);
    return keyEqual(sr.l.key, key);
  }

  @Override
  public Iterator<Map.Entry<double[], V>> iterator() {
    // todo: use snapshots.
    Stack<Node<V>> parents = new Stack<>();
    parents.push(this.root);

    return new Iterator<Map.Entry<double[], V>>() {
      @Override
      public boolean hasNext() {
        return !parents.isEmpty();
      }

      @Override
      public Map.Entry<double[], V> next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        while (!parents.isEmpty()) {
          Node<V> cur = parents.pop();

          if (cur.left == null) {
            if (Double.isFinite(cur.key[0])) {
              return entry(cur);
            }
          } else {
            if (cur.left != null) {
              parents.push(cur.left);
            }

            if (cur.right != null) {
              parents.push(cur.right);
            }
          }
        }

        throw new RuntimeException("Should not happen");
      }

      private Map.Entry<double[], V> entry(Node<V> cur) {
        return new Map.Entry<double[], V>() {
          @Override
          public double[] getKey() {
            return cur.key;
          }

          @Override
          public V getValue() {
            return cur.value;
          }

          // the value of cur is final
          @Override
          public V setValue(V v) {
            return null;
          }
        };
      }
    };
  }

  @Override
  public V get(double[] key) {
    if (contains(key)) {
      final SearchRes sr = search(key);
      return (V) sr.l.value;
    } else {
      return null;
    }
  }

  // sibling is InternalNode
  private void helpMarked2(DeleteInfo<V> info) {
    final Node<V> sibling;

    if (info.l == info.p.left) {
      sibling = info.p.right;
    } else {
      sibling = info.p.left;
    }

    final Node<V> ns =
        new Node<>(sibling.key, info.p.skippedDepth + sibling.skippedDepth + 1, sibling.left,
                   sibling.right);

    // dchild2
    if (info.p == info.gp.left) {
      if (info.gp.CAS_LEFT(info.p, ns)) {
        this.size.getAndDecrement();
      }
    } else {
      if (info.gp.CAS_RIGHT(info.p, ns)) {
        this.size.getAndDecrement();
      }
    }

    // unflag
    info.gp.CAS_INFO(info, new Clean());
  }

  // sibling may be a leaf
  private void helpMarked1(DeleteInfo<V> info) {
    final Node<V> sibling;

    if (info.l == info.p.left) {
      sibling = info.p.right;
    } else {
      sibling = info.p.left;
    }

    if (sibling.left == null) {
      // sibling is Leaf
      final Node<V> ns = new Node<>(sibling.key, sibling.value);

      // dchild1
      if (info.p == info.gp.left) {
        if (info.gp.CAS_LEFT(info.p, ns)) {
          this.size.getAndDecrement();
        }
      } else {
        if (info.gp.CAS_RIGHT(info.p, ns)) {
          this.size.getAndDecrement();
        }
      }

      // unflag
      info.gp.CAS_INFO(info, new Clean());

    } else if (sibling.left != null) {
      // sibling is Internal Node
      // unnecessary to use a wile here, which using infinite loop when inserting a parent.
      final Info sinfo = sibling.info;

      if (sinfo != null && sinfo.getClass() != Clean.class) {
        help(sinfo);
      } else {
        final boolean sresult = sibling.CAS_INFO(sinfo, new Mark2<>(info));
        final Info curSinfo = sibling.info;

        if (sresult ||
            (curSinfo.getClass() == Mark2.class && ((Mark2<V>) curSinfo).deleteInfo == info)) {
          helpMarked2(info);
        } else {
          help(sibling.info);
        }
      }
    }
  }

  private boolean helpDelete(DeleteInfo<V> info) {
    // mark1
    final boolean result = info.p.CAS_INFO(info.pinfo, new Mark1<>(info));
    final Info curPinfo = info.p.info;

    if (result ||
        (curPinfo.getClass() == Mark1.class && ((Mark1<V>) curPinfo).deleteInfo == info)) {
      final Node<V> sibling;

      if (info.l == info.p.left) {
        sibling = info.p.right;
      } else {
        sibling = info.p.left;
      }

      // check sibling
      if (sibling.left == null) {
        // sibling is Leaf
        helpMarked1(info);
        return true;
      } else if (sibling.left != null) {
        // sibling is Internal Node
        final Info sinfo = sibling.info;

        if (sinfo != null && sinfo.getClass() != Clean.class) {
          help(sinfo);
          return false;
        } else {
          final boolean sresult = sibling.CAS_INFO(sinfo, new Mark2<>(info));
          final Info curSinfo = sibling.info;

          if (sresult ||
              (curSinfo.getClass() == Mark2.class && ((Mark2<V>) curSinfo).deleteInfo == info)) {
            helpMarked2(info);
            return true;
          } else {
            help(sibling.info);
            return false;
          }
        }
      }
    } else {
      // backtrack cas
      help(curPinfo);
      info.gp.CAS_INFO(info, new Clean());
      return false;
    }

    throw new RuntimeException("Should not happen");
  }

  private boolean delete(double[] key) {
    while (true) {
      final SearchRes<V> sr = search(key);

      if (sr.p != sr.gp.left && sr.p != sr.gp.right) {
        continue;
      }

      if (sr.l != sr.p.left && sr.l != sr.p.right) {
        continue;
      }

      if (!keyEqual(sr.l.key, key)) {
        return false;
      }

      if (sr.gpinfo != null && sr.gpinfo.getClass() != Clean.class) {
        help(sr.gpinfo);
        continue;
      }

      if (sr.pinfo != null && sr.pinfo.getClass() != Clean.class) {
        help(sr.pinfo);
        continue;
      }

      // dflag
      final DeleteInfo<V> info = new DeleteInfo<>(sr.gp, sr.p, sr.pinfo, sr.l);

      if (sr.gp.CAS_INFO(sr.gpinfo, info)) {
        if (helpDelete(info)) {
          return true;
        } else {
          help(sr.gp.info);
        }
      }
    }
  }

  @Override
  public boolean remove(double[] key) {
    return delete(key);
  }

  @Override
  public int size() {
    return this.size.get();
  }

  public CKDTreeMap<V> snapshot() {
    return null;
  }

  @Override
  public CKDTreeMap<V> clone() {
    return null;
  }
}