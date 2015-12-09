package chaomai.ckdtree.snapshot1;

import chaomai.ckdtree.ICKDTreeMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 12/2/15.
 */

@SuppressWarnings({"unused"})
public final class CKDTreeMap<V> implements ICKDTreeMap<V> {
  private static final AtomicReferenceFieldUpdater<CKDTreeMap, Object> rootUpdater =
      AtomicReferenceFieldUpdater.newUpdater(CKDTreeMap.class, Object.class, "root");
  private final int dimension;
  private volatile Object root;

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

    this.root = new Node<>(key, 0, new Node<>(key, null), new Node<>(key, null), new Gen());
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

  private Object searchKey(double[] key, Gen startGen) {
    Node<V> gp = null;
    Info gpinfo = null;
    Node<V> p = null;
    Info pinfo;
    Node<V> l;

    int depth = 0;
    Node<V> cur = RDCSS_READ_ROOT();

    while (cur.left != null) {
      gp = p;
      p = cur;
      depth += p.skippedDepth;

      if (keyCompare(key, cur.key, depth++) < 0) {
        Node<V> left = cur.GCAS_READ_LEFT(this);

        if (left.left != null && left.gen != startGen) {
          cur.GCAS_LEFT(left, left.renew(startGen, this), this);
          return SearchRes.RESTART;
        }

        cur = p.GCAS_READ_LEFT(this);
      } else {
        Node<V> right = cur.GCAS_READ_RIGHT(this);

        if (right.left != null && right.gen != startGen) {
          cur.GCAS_RIGHT(right, right.renew(startGen, this), this);
          return SearchRes.RESTART;
        }

        cur = p.GCAS_READ_RIGHT(this);
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

  private SearchRes<V> search(double[] key) {
    while (true) {
      Object result = searchKey(key, RDCSS_READ_ROOT().gen);

      if (result != SearchRes.RESTART) {
        return (SearchRes<V>) result;
      }
    }
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

    return new Node<>(maxKey, skip, left, right, RDCSS_READ_ROOT().gen);
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

  private boolean helpInsert(InsertInfo<V> info) {
    final Object result;

    if (info.l == info.p.left) {
      // ichild
      result = info.p.GCAS_LEFT(info.l, info.newInternal, this);
    } else {
      // ichild
      result = info.p.GCAS_RIGHT(info.l, info.newInternal, this);
    }

    if (result == Gen.GenFailed) {
      return false;
    } else {
      // unflag
      info.p.CAS_INFO(info, new Clean());
      return true;
    }
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
        if (helpInsert(info)) {
          return true;
        }
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
    CKDTreeMap<V> snap = snapshot();
    Stack<Node<V>> parents = new Stack<>();
    parents.push(snap.RDCSS_READ_ROOT());

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
  private boolean helpMarked2(DeleteInfo<V> info) {
    final Node<V> sibling;

    if (info.l == info.p.left) {
      sibling = info.p.right;
    } else {
      sibling = info.p.left;
    }

    final Node<V> ns =
        new Node<>(sibling.key, info.p.skippedDepth + sibling.skippedDepth + 1, sibling.left,
                   sibling.right, sibling.gen);

    final Object result;

    if (info.p == info.gp.left) {
      //dchild2
      result = info.gp.GCAS_LEFT(info.p, ns, this);
    } else {
      //dchild2
      result = info.gp.GCAS_RIGHT(info.p, ns, this);
    }

    if (result == Gen.GenFailed) {
      return false;
    } else {
      // unflag
      info.gp.CAS_INFO(info, new Clean());
      return true;
    }
  }

  // sibling may be a leaf
  private boolean helpMarked1(DeleteInfo<V> info) {
    final Node<V> sibling;

    if (info.l == info.p.left) {
      sibling = info.p.right;
    } else {
      sibling = info.p.left;
    }

    if (sibling.left == null) {
      // sibling is Leaf
      final Node<V> ns = new Node<>(sibling.key, sibling.value);

      final Object result;

      if (info.p == info.gp.left) {
        // dchild1
        result = info.gp.GCAS_LEFT(info.p, ns, this);
      } else {
        //dchild2
        result = info.gp.GCAS_RIGHT(info.p, ns, this);
      }

      if (result == Gen.GenFailed) {
        return false;
      } else {
        // unflag
        info.gp.CAS_INFO(info, new Clean());
        return true;
      }

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

      return false;
    }

    throw new RuntimeException("Should not happen");
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
        return helpMarked1(info);
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
            return helpMarked2(info);
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
    // todo: the AtomicInteger size doesn't work well as snapshot2 and snapshot3
    // maybe caused by gcas and gcas_read.
    // any better solution?
    return sequentialSize(RDCSS_READ_ROOT());
  }

  private int sequentialSize(Node node) {
    if (node.left == null) {
      if (Double.isFinite(node.key[0])) {
        return 1;
      } else {
        return 0;
      }
    } else {
      return sequentialSize(node.left) + sequentialSize(node.right);
    }
  }

  private boolean CAS_ROOT(final Object or, final Object desc) {
    return rootUpdater.compareAndSet(this, or, desc);
  }

  private Node<V> RDCSS_Complete(final boolean abort) {
    final Object r = this.root;

    if (r.getClass() == Node.class) {
      return (Node<V>) r;
    } else if (r.getClass() == RDCSSDescriptor.class) {
      final RDCSSDescriptor<V> desc = (RDCSSDescriptor<V>) r;
      final Node<V> or = desc.or;
      final Node<V> ol = desc.ol;
      final Node<V> nr = desc.nr;

      if (abort) {
        if (CAS_ROOT(desc, or)) {
          return or;
        } else {
          return RDCSS_Complete(abort);
        }
      } else {
        Node<V> oldLeaf = or.GCAS_READ_LEFT(this);

        if (oldLeaf == ol) {
          if (CAS_ROOT(desc, nr)) {
            desc.committed = true;
            return nr;
          } else {
            return RDCSS_Complete(abort);
          }
        } else {
          if (CAS_ROOT(desc, or)) {
            return or;
          } else {
            return RDCSS_Complete(abort);
          }
        }
      }
    }

    throw new RuntimeException("Should not happen");
  }

  private boolean RDCSS_ROOT(final Node<V> or, final Node<V> ol, final Node<V> nr) {
    final RDCSSDescriptor<V> desc = new RDCSSDescriptor<>(or, ol, nr);

    if (CAS_ROOT(or, desc)) {
      RDCSS_Complete(false);
      return desc.committed;
    } else {
      return false;
    }
  }

  Node<V> RDCSS_READ_ROOT() {
    return RDCSS_READ_ROOT(false);
  }

  Node<V> RDCSS_READ_ROOT(final boolean abort) {
    final Object r = this.root;

    if (r.getClass() == Node.class) {
      return (Node<V>) r;
    } else if (r.getClass() == RDCSSDescriptor.class) {
      return RDCSS_Complete(abort);
    }

    throw new RuntimeException("Should not happen");
  }

  public CKDTreeMap<V> snapshot() {
    final Node<V> or = RDCSS_READ_ROOT();
    final Node<V> ol = or.GCAS_READ_LEFT(this);

    if (RDCSS_ROOT(or, ol, or.renew(new Gen(), this))) {
      return new CKDTreeMap<>(or.renew(new Gen(), this), this.dimension);
    } else {
      return snapshot();
    }
  }

  @Override
  public CKDTreeMap<V> clone() {
    return null;
  }
}