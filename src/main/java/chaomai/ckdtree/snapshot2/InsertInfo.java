package chaomai.ckdtree.snapshot2;

/**
 * Created by chaomai on 11/22/15.
 */
class InsertInfo<V> extends Info {
  final InternalNode<V> p;
  final InternalNode<V> newInternal;
  final Leaf<V> l;

  InsertInfo(InternalNode<V> p, InternalNode<V> newInternal, Leaf<V> l) {
    this.p = p;
    this.newInternal = newInternal;
    this.l = l;
  }
}