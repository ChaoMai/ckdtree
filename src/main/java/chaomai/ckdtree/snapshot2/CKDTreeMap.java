package chaomai.ckdtree.snapshot2;

import chaomai.ckdtree.ICKDTreeMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chaomai on 11/20/15.
 */

@SuppressWarnings({"unused"})
public class CKDTreeMap<V> implements ICKDTreeMap<V> {
  final InternalNode root;
  private final int dimension;
  private final AtomicInteger size = new AtomicInteger();

  private CKDTreeMap(InternalNode root, int dimension) {
    this.root = root;
    this.dimension = dimension;
  }

  public CKDTreeMap(int dimension) {

    this.dimension = dimension;

    double[] key = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      key[i] = Double.POSITIVE_INFINITY;
    }

    this.root = new InternalNode(key, new Leaf<>(key), new Leaf<>(key), 0);
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
    InternalNode gp = null;
    Update gpupdate = null;
    InternalNode p = null;
    Update pupdate = null;
    Leaf<V> l;
    int depth = 0;

    Node cur = this.root;

    while (cur instanceof InternalNode) {
      gp = p;
      gpupdate = pupdate;
      p = (InternalNode) cur;
      pupdate = p.update;
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

  private InternalNode createSubTree(double[] k, V v, Leaf<V> l, int depth) {
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

    return new InternalNode(maxKey, left, right, skip);
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

      InternalNode newInternal = createSubTree(key, value, r.l, r.leafDepth);

      // iflag
      InsertInfo<V> info = new InsertInfo<>(r.p, newInternal, r.l);
      Update iu = new Update(State.IFLAG, info);

      if (r.p.CAS_UPDATE(r.pupdate, iu)) {
        helpInsert(iu);
        return true;
      } else {
        Update update = r.p.update;
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

  @Override
  public Iterator<Map.Entry<double[], V>> iterator() {
    return null;
  }

  @Override
  public V get(Object key) {
    return null;
  }

  // sibling is InternalNode
  private void helpMarked2(Update m2u) {
    DeleteInfo<V> info = (DeleteInfo<V>) m2u.info;

    InternalNode sibling;

    if (info.l == info.p.left) {
      sibling = (InternalNode) info.p.right;
    } else {
      sibling = (InternalNode) info.p.left;
    }

    InternalNode ns = new InternalNode(sibling.key, sibling.left, sibling.right,
                                       info.p.skippedDepth + sibling.skippedDepth +
                                       1);

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
    info.gp.CAS_UPDATE(info.gp.update, new Update());
  }

  // sibling is leaf
  private void helpMarked1(Update m1u) {
    DeleteInfo<V> info = (DeleteInfo<V>) m1u.info;

    Node sibling;

    if (info.l == info.p.left) {
      sibling = info.p.right;
    } else {
      sibling = info.p.left;
    }

    if (sibling instanceof Leaf) {
      Leaf<V> ns = new Leaf<>(sibling.key, ((Leaf<V>) sibling).value);

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
      info.gp.CAS_UPDATE(info.gp.update, new Update());

    } else if (sibling instanceof InternalNode) {
      Update supdate = ((InternalNode) sibling).update;

      if (supdate.state == State.CLEAN) {
        Update m2u = new Update(State.MARK2, info);
        boolean sresult = ((InternalNode) sibling).CAS_UPDATE(supdate, m2u);

        if ((supdate.state == State.CLEAN && sresult) ||
            (supdate.state == State.MARK2 && supdate.info == info)) {
          helpMarked2(m2u);
        } else {
          help(((InternalNode) sibling).update);
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

    Update update = info.p.update;

    if (result || (update.state == State.MARK1 && update.info == info)) {
      Node sibling;

      if (info.l == info.p.left) {
        sibling = info.p.right;
      } else {
        sibling = info.p.left;
      }

      // check sibling
      if (sibling instanceof Leaf) {
        helpMarked1(m1u);
        return true;
      } else if (sibling instanceof InternalNode) {
        Update supdate = ((InternalNode) sibling).update;

        if (supdate.state == State.CLEAN) {
          Update m2u = new Update(State.MARK2, info);
          boolean sresult = ((InternalNode) sibling).CAS_UPDATE(supdate, m2u);

          if ((supdate.state == State.CLEAN && sresult) ||
              (supdate.state == State.MARK2 && supdate.info == info)) {
            helpMarked2(m2u);
            return true;
          } else {
            help(((InternalNode) sibling).update);
            return false;
          }
        } else {
          help(supdate);
          return false;
        }
      }
    } else {
      help(update);

      // backtrack cas
      info.gp.CAS_UPDATE(info.gp.update, new Update());

      return false;
    }

    throw new RuntimeException("Should not happen");
  }

  private boolean delete(double[] key) {
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
          Update update = r.p.update;
          help(update);
        }
      }
    }
  }

  @Override
  public boolean remove(double[] key) {
    return delete(key);
  }

  // todo: snapshot?
  @Override
  public int size() {
    return this.size.get();
  }

  private void readRefs(final Node node, final HashMap<InternalNode, Pair<Node, Node>> refs) {
    if (node instanceof Leaf) {
      return;
    }

    InternalNode in = (InternalNode) node;

    refs.put(in, new Pair<>(in.left, in.right));
    readRefs(in.left, refs);
    readRefs(in.right, refs);
  }

  private boolean checkRefs(final Node node, final HashMap<InternalNode, Pair<Node, Node>> refs) {
    if (node instanceof Leaf) {
      return true;
    }

    InternalNode in = (InternalNode) node;
    Pair<Node, Node> pair = refs.get(in);

    if (!pair.equals(new Pair<>(in.left, in.right))) {
      return false;
    }
    return checkRefs(in.left, refs) && checkRefs(in.right, refs);
  }

  private Node buildRefs(final Node node, final HashMap<InternalNode, Pair<Node, Node>> refs) {
    if (node instanceof Leaf) {
      return node;
    }

    InternalNode in = (InternalNode) node;
    Pair<Node, Node> pair = refs.get(in);

    return new InternalNode(in.key, buildRefs(pair.key, refs), buildRefs(pair.value, refs));
  }

  private InternalNode getSnapshot() {
    final HashMap<InternalNode, Pair<Node, Node>> refs = new HashMap<>();
    readRefs(root, refs);

    if (!checkRefs(root, refs)) {
      return null;
    }

    return (InternalNode) buildRefs(root, refs);
  }

  public CKDTreeMap<V> snapshot() {
    return new CKDTreeMap<>(getSnapshot(), this.dimension);
  }
}