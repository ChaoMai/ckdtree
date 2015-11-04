package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/3/15.
 */
class FailedNode<V> extends Node {
  volatile InternalNode<V> prev;

  static final AtomicReferenceFieldUpdater<FailedNode, Node> prevUpdater =
      AtomicReferenceFieldUpdater.newUpdater(FailedNode.class, Node.class, "prev");

  FailedNode(Node prev) {
    super(null);
    prevUpdater.set(this, prev);
  }
}