package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/1/15.
 */
class InternalNode<V> extends Node {
  volatile Node left;
  volatile Node right;
  volatile Update update;
  final int skippedDepth;
  final Gen gen;
  volatile Node prev;

  private static final AtomicReferenceFieldUpdater<InternalNode, Node> leftUpdater = AtomicReferenceFieldUpdater
      .newUpdater(InternalNode.class, Node.class, "left");

  private static final AtomicReferenceFieldUpdater<InternalNode, Node> rightUpdater = AtomicReferenceFieldUpdater
      .newUpdater(InternalNode.class, Node.class, "right");

  private static final AtomicReferenceFieldUpdater<InternalNode, Update> updateUpdater = AtomicReferenceFieldUpdater
      .newUpdater(InternalNode.class, Update.class, "update");

  private static final AtomicReferenceFieldUpdater<InternalNode, Node> prevUpdater = AtomicReferenceFieldUpdater
      .newUpdater(InternalNode.class, Node.class, "prev");

  InternalNode(double[] key, Node left, Node right, Gen gen) {
    this(key, left, right, new Update(), 0, gen);
  }

  InternalNode(double[] key, Node left, Node right, Update update, int skippedDepth, Gen gen) {
    super(key);
    this.left = left;
    this.right = right;
    this.update = update;
    this.skippedDepth = skippedDepth;
    this.gen = gen;
    this.prev = null;
  }

  InternalNode<V> renewed(Gen newGen) {
    // todo: should perform a deep copy here(everything here and things in `update`)
    // todo: or just create new `update`
    // todo: any optimization?
    return new InternalNode<V>(key, left, right, new Update(), skippedDepth, gen);
  }

  private boolean CAS_LEFT(InternalNode<V> old, InternalNode<V> n) {
    return leftUpdater.compareAndSet(this, old, n);
  }

  private boolean CAS_RIGHT(InternalNode<V> old, InternalNode<V> n) {
    return rightUpdater.compareAndSet(this, old, n);
  }

  boolean CAS_UPDATE(Update old, Update n) {
    return updateUpdater.compareAndSet(this, old, n);
  }

  Update GET_UPDATE() {
    return updateUpdater.get(this);
  }

  private boolean CAS_PREV(Node old, Node n) {
    return prevUpdater.compareAndSet(this, old, n);
  }

  private void WRITE_PREV(InternalNode<V> old) {
    prevUpdater.set(this, old);
  }

  private InternalNode<V> GCAS_Complete(InternalNode<V> n, CKDTreeMap<V> ckd, Direction direction) {
    if (n == null) {
      return null;
    } else {
      Node prev = n.prev;
      InternalNode<V> root = ckd.readRoot();

      if (prev == null) {
        return n;
      }

      if (prev instanceof FailedNode) {
        FailedNode<V> fn = (FailedNode<V>) prev;
        if (direction == Direction.LEFT) {
          if (CAS_LEFT(n, fn.prev)) {
            return fn.prev;
          } else {
            // todo: is it necessary to GCAS_Complete(left, ...) or GCAS_Complete(left, ...)?
            return GCAS_Complete(n, ckd, direction);
          }
        } else {
          if (CAS_RIGHT(n, fn.prev)) {
            return fn.prev;
          } else {
            // todo: is it necessary to GCAS_Complete(left, ...) or GCAS_Complete(right, ...)?
            return GCAS_Complete(n, ckd, direction);
          }
        }
      } else if (prev instanceof InternalNode) {
        if (root.gen == gen && ckd.nonReadOnly()) {
          if (n.CAS_PREV(prev, null)) {
            return n;
          } else {
            return GCAS_Complete(n, ckd, direction);
          }
        } else {
          n.CAS_PREV(prev, new FailedNode<V>(prev));
          // todo: is it necessary to GCAS_Complete(left, ...) or GCAS_Complete(right, ...)?
          return GCAS_Complete(n, ckd, direction);
        }
      }
    }
    throw new RuntimeException("Should not happen");
  }

  // todo: direction is ugly here, any better idea?
  boolean GCAS(InternalNode<V> old, InternalNode<V> n, CKDTreeMap<V> ckd, Direction direction) {
    if (direction == Direction.LEFT) {
      n.WRITE_PREV(old);
      if (CAS_LEFT(old, n)) {
        GCAS_Complete(n, ckd, direction);
        return n.prev == null;
      } else {
        return false;
      }
    } else {
      n.WRITE_PREV(old);
      if (CAS_RIGHT(old, n)) {
        GCAS_Complete(n, ckd, direction);
        return n.prev == null;
      } else {
        return false;
      }
    }
  }
}