package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class InsertInfo<V> extends Info {
  InternalNode p;
  InternalNode newInternal;
  Leaf<V> l;

  InsertInfo(InternalNode p, InternalNode newInternal, Leaf<V> l) {
    this.p = p;
    this.newInternal = newInternal;
    this.l = l;
  }
}