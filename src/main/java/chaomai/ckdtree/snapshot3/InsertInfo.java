package chaomai.ckdtree.snapshot3;

/**
 * Created by chaomai on 12/2/15.
 */
class InsertInfo<V> extends Info {
  final Node<V> p;
  final Node<V> newInternal;
  final Node<V> l;

  InsertInfo(Node<V> p, Node<V> newInternal, Node<V> l) {
    this.p = p;
    this.newInternal = newInternal;
    this.l = l;
  }
}