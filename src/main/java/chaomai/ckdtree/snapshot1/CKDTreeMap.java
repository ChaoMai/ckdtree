package chaomai.ckdtree.snapshot1;

import chaomai.ckdtree.ICKDTreeMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/1/15.
 */

@SuppressWarnings({"unused"})
public class CKDTreeMap<V> implements ICKDTreeMap<V> {
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

    this.root = new InternalNode<>(key, new Leaf<>(key), new Leaf<>(key), 0, new Gen());
  }

  public CKDTreeMap(int dimension) {
    this(false, dimension);
  }

  private InternalNode<V> RDCSS_COMPLETE(boolean abort) {
    while (true) {
      Object v = this.root;

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
    Object r = this.root;

    if (r instanceof InternalNode) {
      return (InternalNode<V>) r;
    } else {
      return RDCSS_COMPLETE(abort);
    }
  }

  boolean isReadOnly() {
    return readOnly;
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
    InternalNode<V> p = null;
    Update pupdate = null;
    Leaf<V> l;
    int depth = 0;

    Node<V> cur = this.RDCSS_READ_ROOT();

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
          // use startGen instead of root's gen, since the gen of root may change while perform
          // searching. if the gen of root is used, searchKey will generate a branch at some internal
          // node after the root's gen is changed.
          if (left.gen != startGen) {
            // do GCAS, change the left into a new one with new gen.
            cur.GCAS(left, ((InternalNode<V>) left).renewed(startGen, this), this, Direction.LEFT);
            // retry
            return SearchRes.RESTART;
          }
        }

        cur = p.GCAS_READ_LEFT_CHILD(this);

      } else {
        // if right child are InternalNode, then check their generation.
        Node<V> right = cur.GCAS_READ_RIGHT_CHILD(this);

        if (right instanceof InternalNode) {
          if (right.gen != startGen) {
            cur.GCAS(right, ((InternalNode<V>) right).renewed(startGen, this), this,
                     Direction.RIGHT);
            return SearchRes.RESTART;
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

    if (info.l == info.p.GCAS_READ_LEFT_CHILD(this)) {
      // ichild
      if (info.p.GCAS(info.l, info.newInternal, this, Direction.LEFT)) {
        this.size.getAndIncrement();
      }

      // unflag
      info.p.CAS_UPDATE(iu, new Update());

    } else {
      // ichild
      if (info.p.GCAS(info.l, info.newInternal, this, Direction.RIGHT)) {
        this.size.getAndIncrement();
      }

      // unflag
      info.p.CAS_UPDATE(iu, new Update());
    }
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
      InsertInfo<V> info = new InsertInfo<>(r.p, newInternal, r.l);
      Update iu = new Update(State.IFLAG, info);

      if (r.p.CAS_UPDATE(r.pupdate, iu)) {
        helpInsert(iu);
        return true;
      } else {
        Update update = r.p.GET_UPDATE();
        help(update);
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

  //   since concurrent operations change the structure of the tree and increase the complexity of iteration,
  //   the iterator is readonly.
  @Override
  public Iterator<Map.Entry<double[], V>> iterator() {
    CKDTreeMap<V> readOnlySnapshotCKD = readOnlySnapshot();
    Node<V> nextNode = readOnlySnapshotCKD.RDCSS_READ_ROOT();

    Stack<Node<V>> parents = new Stack<>();
    parents.push(readOnlySnapshotCKD.RDCSS_READ_ROOT());

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

        V key;

        // since the ckd now is readonly, just directly access two child fields.
        Node<V> cur;

        while (!parents.isEmpty()) {
          cur = parents.pop();

          if (cur instanceof Leaf) {
            if (!Double.isInfinite(cur.key[0])) {
              return entry((Leaf<V>) cur);
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

      private Map.Entry<double[], V> entry(Leaf<V> cur) {
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
  public V get(Object key) {
    return null;
  }

  // sibling is InternalNode
  private void helpMarked2(Update m2u) {
    DeleteInfo<V> info = (DeleteInfo<V>) m2u.info;

    Node<V> sibling;

    if (info.l == info.p.GCAS_READ_LEFT_CHILD(this)) {
      sibling = info.p.GCAS_READ_RIGHT_CHILD(this);
    } else {
      sibling = info.p.GCAS_READ_LEFT_CHILD(this);
    }

    InternalNode<V> newSibling = new InternalNode<>(sibling.key, sibling.GCAS_READ_LEFT_CHILD(this),
                                                    sibling.GCAS_READ_RIGHT_CHILD(this),
                                                    info.p.skippedDepth +
                                                    ((InternalNode<V>) sibling).skippedDepth + 1,
                                                    sibling.gen);

    Direction direction;

    if (info.p == info.gp.GCAS_READ_LEFT_CHILD(this)) {
      direction = Direction.LEFT;
    } else {
      direction = Direction.RIGHT;
    }

    // dchild2
    if (info.gp.GCAS(info.p, newSibling, this, direction)) {
      this.size.getAndDecrement();
    }

    // unflag
    info.gp.CAS_UPDATE(info.gp.GET_UPDATE(), new Update());
  }

  // sibling is leaf
  private void helpMarked1(Update m1u) {
    DeleteInfo<V> info = (DeleteInfo<V>) m1u.info;

    Node<V> sibling;

    if (info.l == info.p.GCAS_READ_LEFT_CHILD(this)) {
      sibling = info.p.GCAS_READ_RIGHT_CHILD(this);
    } else {
      sibling = info.p.GCAS_READ_LEFT_CHILD(this);
    }

    // although the type of sibling seems to be known before this method, it just happens when there
    // is only one thread.
    // when multiple threads exist, other find marked parent won't know the type of sibling.

    if (sibling instanceof Leaf) {
      Leaf<V> ns = new Leaf<>(sibling.key, ((Leaf<V>) sibling).value);

      Direction direction;

      if (info.p == info.gp.GCAS_READ_LEFT_CHILD(this)) {
        direction = Direction.LEFT;
      } else {
        // since the right child may also be compared, so the root must hold a valid reference to a right child.
        direction = Direction.RIGHT;
      }

      // dchild1
      if (info.gp.GCAS(info.p, ns, this, direction)) {
        this.size.getAndDecrement();
      }

      // unflag
      info.gp.CAS_UPDATE(info.gp.GET_UPDATE(), new Update());

    } else if (sibling instanceof InternalNode) {
      Update supdate = ((InternalNode) sibling).GET_UPDATE();

      if (supdate.state == State.CLEAN) {
        Update m2u = new Update(State.MARK2, info);
        if (((InternalNode) sibling).CAS_UPDATE(supdate, m2u)) {
          helpMarked2(m1u);
        } else {
          help(supdate);
        }
      } else {
        help(supdate);
      }
    }
  }

  private boolean helpDelete(Update du) {
    DeleteInfo<V> info = (DeleteInfo<V>) du.info;

    // mark1
    Update m1u = new Update(State.MARK1, info);

    boolean result = info.p.CAS_UPDATE(info.pupdate, m1u);

    Update update = info.p.GET_UPDATE();

    if (result || (update.state == State.MARK1 && update.info == info)) {
      // sibling of parent's child should be obtained after parent marked.
      // since before parent parked, children aren't stable.
      Node<V> sibling;

      if (info.l == info.p.GCAS_READ_LEFT_CHILD(this)) {
        sibling = info.p.GCAS_READ_RIGHT_CHILD(this);
      } else {
        sibling = info.p.GCAS_READ_LEFT_CHILD(this);
      }

      // check sibling
      if (sibling instanceof Leaf) {
        helpMarked1(m1u);
        return true;
      } else if (sibling instanceof InternalNode) {
        // since the sibling is InternalNode, it may not be CLEAN.
        Update supdate = ((InternalNode) sibling).GET_UPDATE();

        if (supdate.state == State.CLEAN) {
          Update m2u = new Update(State.MARK2, info);

          if (((InternalNode) sibling).CAS_UPDATE(supdate, m2u)) {
            helpMarked2(m2u);
            return true;
          } else {
            help(supdate);
            return false;
          }
        } else {
          help(supdate);
          return false;
        }
      } else {
        throw new RuntimeException("Should not happen");
      }
    } else {
      help(update);

      // backtrack cas
      info.gp.CAS_UPDATE(info.gp.GET_UPDATE(), new Update());

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
      DeleteInfo<V> info = new DeleteInfo<>(r.gp, r.p, r.pupdate, r.l);
      Update du = new Update(State.DFLAG, info);

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

  @Override
  public boolean remove(double[] key) {
    return delete(key);
  }

  // todo: fix this, size in snapshot won't work.
  @Override
  public int size() {
    return this.size.get();
  }

  // 1.
  // it's necessary to make sure root and its left child not changed when updating the root to new gen.
  // otherwise, insertion may lost at some scenarios.
  @Override
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
    if (isReadOnly()) {
      return this;
    }

    while (true) {
      InternalNode<V> r = RDCSS_READ_ROOT();
      Node<V> ol = r.GCAS_READ_LEFT_CHILD(this);

      InternalNode<V> nr = r.copyRootToGen(new Gen(), this);

      if (RDCSS_ROOT(r, ol, nr)) {
        return new CKDTreeMap<>(r, true, this.dimension);
      } else {
        continue;
      }
    }
  }

  @Override
  public String toString() {
    return this.RDCSS_READ_ROOT().toString();
  }
}