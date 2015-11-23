package chaomai.ckdtree.snapshot2;

/**
 * Created by chaomai on 11/22/15.
 */
class InsertInfo<V> extends Info {
  final InternalNode p;
  final InternalNode newInternal;
  final Leaf<V> l;

  InsertInfo(InternalNode p, InternalNode newInternal, Leaf<V> l) {
    this.p = p;
    this.newInternal = newInternal;
    this.l = l;
  }
}