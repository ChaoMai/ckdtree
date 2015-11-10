package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class InsertInfo<V> extends Info {
  InternalNode<V> p;
  InternalNode<V> newInternal;
  Leaf<V> l;

  InsertInfo(InternalNode<V> p, InternalNode<V> newInternal, Leaf<V> l) {
    this.p = p;
    this.newInternal = newInternal;
    this.l = l;
  }
}