package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/3/15.
 */
class FailedNode<V> extends Node {
  InternalNode<V> prev;

  public static final AtomicReferenceFieldUpdater<FailedNode, InternalNode> prevUpdater =
      AtomicReferenceFieldUpdater.newUpdater(FailedNode.class, InternalNode.class, "prev");

  FailedNode(InternalNode<V> p) {
    super(null);
    prevUpdater.set(this, p);
  }
}