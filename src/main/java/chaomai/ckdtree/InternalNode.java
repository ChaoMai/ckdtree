package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/1/15.
 */
class InternalNode<V> extends Node {
  Node left;
  Node right;
  final Update update;
  final int skippedDepth;
  final Gen gen;

  Node prev;

  public static final AtomicReferenceFieldUpdater<InternalNode, Node> prevUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Node.class, "prev");

  public static final AtomicReferenceFieldUpdater<InternalNode, InternalNode> leftUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, InternalNode.class, "left");

  public static final AtomicReferenceFieldUpdater<InternalNode, InternalNode> rightUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, InternalNode.class, "right");

  InternalNode(double[] key, Node left, Node right, Update update, int skippedDepth, Gen gen) {
    super(key);
    this.left = left;
    this.right = right;
    this.update = update;
    this.skippedDepth = skippedDepth;
    this.gen = gen;
  }

  InternalNode<V> renewed(Gen newGen) {
    // todo: should perform a deep copy here(everything here and things in `update`),
    // todo: or just create new `update`,
    // todo: any optimization?
    return new InternalNode<V>(key, left, right, new Update(), skippedDepth, gen);
  }

  boolean CAS_LEFT(InternalNode<V> old, InternalNode<V> n) {
    return leftUpdater.compareAndSet(this, old, n);
  }

  boolean CAS_RIGHT(InternalNode<V> old, InternalNode<V> n) {
    return rightUpdater.compareAndSet(this, old, n);
  }

  boolean CAS_PREV(Node old, Node n) {
    return prevUpdater.compareAndSet(this, old, n);
  }

  void WRITE_PREV(InternalNode<V> old) {
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
      } else if (prev instanceof FailedNode) {
//        FailedNode<V> fn = (FailedNode<V>) prev;
//        if (CAS_LEFT(n, fn.prev)){
//          return fn.prev;
//        }else{
//          GCAS_Complete()
//        }
      } else if (prev instanceof InternalNode) {
        if (root.gen == gen && ckd.nonReadOnly()) {
          if (n.CAS_PREV(prev, null)) {
            return n;
          } else {
            n.CAS_PREV(prev, new FailedNode<V>(prev));
            GCAS_Complete(n, ckd, direction);
          }
        }
      }
    }

//    while (true) {
//      if (n == null) {
//        return null;
//      } else {
//        InternalNode<V> prev = n.prev;
//        InternalNode<V> root = ckd.readRoot();
//
//        if (prev == null) {
//          return n;
//        } else if (prev instanceof InternalNode && ckd.nonReadOnly()) {
//          if (root.gen == this.gen) {
//            if (n.CAS_PREV(prev, null)) {
//              return n;
//            } else {
//              continue;
//            }
//          }
//        } else {
//          //
//        }
//      }
//    }
  }

  // todo: direction is ugly here, any better idea.
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