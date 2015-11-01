package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class IInfo<V> {
  InternalNode p;
  InternalNode newInternal;
  Leaf<V> l;

  IInfo(InternalNode p, InternalNode newInternal, Leaf<V> l) {
    this.p = p;
    this.newInternal = newInternal;
    this.l = l;
  }
}
