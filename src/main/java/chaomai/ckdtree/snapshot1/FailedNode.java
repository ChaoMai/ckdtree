package chaomai.ckdtree.snapshot1;

/**
 * Created by chaomai on 11/3/15.
 */
class FailedNode<V> extends Node<V> {
  FailedNode(Node<V> prev) {
    super();
    WRITE_PREV(prev);
  }
}