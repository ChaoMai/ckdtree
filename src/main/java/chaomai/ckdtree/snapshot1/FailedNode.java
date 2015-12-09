package chaomai.ckdtree.snapshot1;

/**
 * Created by chaomai on 12/8/15.
 */
class FailedNode<V> extends Node<V> {
  FailedNode(final Node<V> old) {
    super(null, null);
    WRITE_PREV(old);
  }
}