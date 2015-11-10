package chaomai.ckdtree;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/1/15.
 */

@SuppressWarnings({"unused"})
public class CKDTreeMap<V> {
  private final int dimension;
  private final boolean readOnly;
  private final AtomicInteger size = new AtomicInteger();
  private volatile Object root;

  private AtomicReferenceFieldUpdater<CKDTreeMap, Object> rootUpdater =
      AtomicReferenceFieldUpdater.newUpdater(CKDTreeMap.class, Object.class, "root");

  private CKDTreeMap(InternalNode<V> root, boolean readOnly, int dimension) {
    this.root = root;
    this.readOnly = readOnly;
    this.dimension = dimension;
  }

  private CKDTreeMap(boolean readOnly, int dimension) {
    this.readOnly = readOnly;
    this.dimension = dimension;

    double[] key = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      key[i] = Double.POSITIVE_INFINITY;
    }

    this.root = new InternalNode<>(key, new Leaf<>(key), null, 0, new Gen());
  }

  public CKDTreeMap(int dimension) {
    this(false, dimension);
  }

  private InternalNode<V> RDCSS_COMPLETE(boolean abort) {
    while (true) {
      Object v = root;

      if (v instanceof InternalNode) {
        return (InternalNode<V>) v;
      } else if (v instanceof RDCSSDescriptor) {
        RDCSSDescriptor<V> desc = (RDCSSDescriptor<V>) v;

        InternalNode<V> ov = desc.ov;
        Node<V> expl = desc.ol;
        InternalNode<V> nv = desc.nv;

        if (abort) {
          if (CAS_ROOT(desc, ov)) {
            return ov;
          } else {
            continue;
          }
        } else {
          Node<V> ol = ov.GCAS_READ_LEFT_CHILD(this);

          if (ol == expl) {
            if (CAS_ROOT(desc, nv)) {
              desc.committed = true;
              return nv;
            } else {
              continue;
            }
          } else {
            if (CAS_ROOT(desc, ov)) {
              return ov;
            } else {
              continue;
            }
          }
        }
      }
    }
  }

  private boolean RDCSS_ROOT(InternalNode<V> ov, Node<V> ol, InternalNode<V> nv) {
    RDCSSDescriptor<V> desc = new RDCSSDescriptor<>(ov, ol, nv);

    if (CAS_ROOT(ov, desc)) {
      RDCSS_COMPLETE(false);
      return desc.committed;
    } else {
      return false;
    }
  }

  InternalNode<V> RDCSS_READ_ROOT() {
    return RDCSS_READ_ROOT(false);
  }

  InternalNode<V> RDCSS_READ_ROOT(boolean abort) {
    Object r = root;

    if (r instanceof InternalNode) {
      return (InternalNode<V>) r;
    } else {
      return RDCSS_COMPLETE(abort);
    }
  }

  boolean isReadOnly() {
    return readOnly;
  }

  boolean nonReadOnly() {
    return !readOnly;
  }

  private boolean CAS_ROOT(Object old, Object n) {
    if (isReadOnly()) {
      throw new IllegalStateException("Attempted to modify a read-only snapshot");
    }

    return rootUpdater.compareAndSet(this, old, n);
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

  private Object searchKey(double[] key, Gen startGen) {
    InternalNode<V> gp = null;
    Update gpupdate = null;
    InternalNode<V> p = this.RDCSS_READ_ROOT();
    Update pupdate = p.GET_UPDATE();
    Leaf<V> l;
    int depth = 0;

    Node<V> cur = p.GCAS_READ_LEFT_CHILD(this);

    while (cur instanceof InternalNode) {
      // continue searching
      gp = p;
      gpupdate = pupdate;
      p = (InternalNode<V>) cur;
      pupdate = p.GET_UPDATE();
      depth += p.skippedDepth;

      if (keyCompare(key, cur.key, depth++) < 0) {
        // if left child are InternalNode, then check their generation.
        Node<V> left = cur.GCAS_READ_LEFT_CHILD(this);

        // only perform GCAS on InternalNode
        if (left instanceof InternalNode) {
          if (left.gen != startGen) {
            // do GCAS, change the left into a new with new gen.
            if (cur.GCAS(left, ((InternalNode<V>) left).renewed(startGen, this), this,
                         Direction.LEFT)) {
              // retry on cur
              continue;
            } else {
              return SearchRes.RESTART;
            }
          }
        }

        cur = p.GCAS_READ_LEFT_CHILD(this);

      } else {
        // if right child are InternalNode, then check their generation.
        Node<V> right = cur.GCAS_READ_RIGHT_CHILD(this);

        if (right instanceof InternalNode) {
          if (((InternalNode) right).gen != startGen) {
            if (cur.GCAS(right, ((InternalNode<V>) right).renewed(startGen, this), this,
                         Direction.RIGHT)) {
              continue;
            } else {
              return SearchRes.RESTART;
            }
          }
        }

        cur = p.GCAS_READ_RIGHT_CHILD(this);
      }
    }

    l = (Leaf<V>) cur;

    return new SearchRes<>(gp, gpupdate, p, pupdate, l, depth);
  }

  SearchRes<V> search(double[] key) {
    while (true) {
      Object res = searchKey(key, this.RDCSS_READ_ROOT().gen);

      if (res == SearchRes.RESTART) {
        continue;
      } else {
        return (SearchRes<V>) res;
      }
    }
  }

  private InternalNode<V> createSubTree(double[] k, V v, Leaf<V> l, int depth) {
    int skip = 0;
    int compareResult;
    while ((compareResult = keyCompare(k, l.key, depth++)) == 0) {
      ++skip;
    }

    Leaf<V> left;
    Leaf<V> right;
    double[] maxKey;

    if (compareResult < 0) {
      maxKey = l.key;
      left = new Leaf<>(k, v);
      right = new Leaf<>(l.key, l.value);
    } else {
      maxKey = k;
      left = new Leaf<>(l.key, l.value);
      right = new Leaf<>(k, v);
    }

    return new InternalNode<>(maxKey, left, right, skip, this.RDCSS_READ_ROOT().gen);
  }

  private void help(Update update) {
    switch (update.state) {
      case IFLAG: {
        helpInsert(update);
        break;
      }
      case DFLAG: {
        helpDelete(update);
        break;
      }
      case MARK1: {
        helpMarked1(update);
        break;
      }
      case MARK2: {
        helpMarked2(update);
        break;
      }
    }
  }

  private void helpInsert(Update iu) {
    InsertInfo<V> info = (InsertInfo<V>) iu.info;

    // ichild
    if (keyCompare(info.newInternal.key, info.p.key, iu.depth) < 0) {
      info.p.GCAS(info.l, info.newInternal, this, Direction.LEFT);
    } else {
      info.p.GCAS(info.l, info.newInternal, this, Direction.RIGHT);
    }

    size.getAndIncrement();

    // unflag
    info.p.CAS_UPDATE(iu, new Update());
  }

  private boolean insert(double[] key, V value) {
    while (true) {
      SearchRes<V> r = search(key);

      if (keyEqual(r.l.key, key)) {
        return false;
      }

      if (r.pupdate.state != State.CLEAN) {
        help(r.pupdate);
        continue;
      }

      InternalNode<V> newInternal = createSubTree(key, value, r.l, r.leafDepth);

      // iflag
      InsertInfo<V> op = new InsertInfo<>(r.p, newInternal, r.l);
      Update iu = new Update(State.IFLAG, op, /*use depth of leaf's parent*/ --r.leafDepth);

      if (r.p.CAS_UPDATE(r.pupdate, iu)) {
        helpInsert(iu);
        return true;
      } else {
        // help
        Update update = r.p.GET_UPDATE();
        help(update);
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

  // sibling is InternalNode
  private void helpMarked2(Update m2u) {

  }

  // sibling is leaf
  private void helpMarked1(Update m1u) {
    DeleteInfo<V> info = (DeleteInfo<V>) m1u.info;

    if (info.gp.GCAS(info.p, info.sibling, this, info.siblingDirection)) {
      this.size.getAndDecrement();

      // unflag
      info.gp.CAS_UPDATE(m1u, new Update());
    }
  }

  private boolean helpDelete(Update du) {
    DeleteInfo<V> info = (DeleteInfo<V>) du.info;

    // mark1
    Update m1u = new Update(State.MARK1, info, du.depth);
    if (info.p.CAS_UPDATE(info.pupdate, m1u)) {
      helpMarked1(m1u);

      // check sibling
      if (info.sibling instanceof Leaf || info.sibling == null) {
        helpMarked1(m1u);
        return true;
      } else if (info.sibling instanceof InternalNode) {
        // since the sibling is InternalNode, it may be not CLEAN.
        Update update = ((InternalNode) info.sibling).GET_UPDATE();
        if (update.state == State.CLEAN) {
          helpMarked2(m1u);
        } else {
          help(update);
        }

        return true;
      } else {
        throw new RuntimeException("Should not happen");
      }
    } else {
      // help
      Update update = info.p.GET_UPDATE();
      help(update);

      // todo: finish this
      // backtrack cas

      return false;
    }
  }

  boolean delete(double[] key) {
    while (true) {
      SearchRes<V> r = search(key);

      if (!keyEqual(r.l.key, key)) {
        return false;
      }

      if (r.gpupdate.state != State.CLEAN) {
        help(r.gpupdate);
        continue;
      }

      if (r.pupdate.state != State.CLEAN) {
        help(r.pupdate);
        continue;
      }

      // dflag
      Node<V> sibling;
      Direction siblingDirection;
      int parentDepth = r.leafDepth - 1;
      if (keyCompare(r.l.key, r.p.key, parentDepth) < 0) {
        sibling = r.p.GCAS_READ_RIGHT_CHILD(this);
        // todo: check the direction
        siblingDirection = Direction.LEFT;
      } else {
        sibling = r.p.GCAS_READ_LEFT_CHILD(this);
        siblingDirection = Direction.RIGHT;
      }

      DeleteInfo<V> op = new DeleteInfo<>(r.gp, r.p, sibling, siblingDirection, r.l, r.pupdate);
      Update du = new Update(State.DFLAG, op, r.leafDepth);

      if (r.gp.CAS_UPDATE(r.gpupdate, du)) {
        if (helpDelete(du)) {
          return true;
        } else {
          continue;
        }
      } else {
        Update update = r.p.GET_UPDATE();
        help(update);
      }
    }
  }

  public boolean remove(double[] key) {
    return false;
  }

  public int size() {
    return this.size.get();
  }

  // 1.
  // to keep invariant from root, both root and its left child must be updated.
  // otherwise, when iteration reaches the left child of root and decide to change its left child to new,
  // the gcas operation will be bound to fail. because the gen of the left child of root is old.
  //
  // 2.
  // it's necessary to make sure root and its left child not changed when updating the root to new gen.
  // otherwise, insertion may lost at some scenarios.
  public CKDTreeMap<V> snapshot() {
    while (true) {
      InternalNode<V> r = RDCSS_READ_ROOT();
      Node<V> ol = r.GCAS_READ_LEFT_CHILD(this);

      InternalNode<V> nr = r.copyRootToGen(new Gen(), this);

      if (RDCSS_ROOT(r, ol, nr)) {
        InternalNode<V> snap = r.copyRootToGen(new Gen(), this);
        return new CKDTreeMap<>(snap, this.readOnly, this.dimension);
      } else {
        continue;
      }
    }
  }

  public CKDTreeMap<V> readOnlySnapshot() {
    return null;
  }

  @Override
  public String toString() {
    return this.RDCSS_READ_ROOT().toString();
  }
}