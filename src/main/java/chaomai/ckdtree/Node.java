package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/1/15.
 */
abstract class Node {
  final double[] key;
  volatile Node prev;

  Node(double[] key) {
    this.key = key;
  }

  private static final AtomicReferenceFieldUpdater<Node, Node> prevUpdater = AtomicReferenceFieldUpdater
      .newUpdater(Node.class, Node.class, "prev");

  private boolean CAS_PREV(Node old, Node n) {
    return prevUpdater.compareAndSet(this, old, n);
  }

  private void WRITE_PREV(Node old) {
    prevUpdater.set(this, old);
  }
}