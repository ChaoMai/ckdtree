package chaomai.ckdtree.snapshot2;

/**
 * Created by chaomai on 11/23/15.
 */
class DeleteInfo<V> extends Info {
  final InternalNode<V> gp;
  final InternalNode<V> p;
  final Update pupdate;
  final Leaf<V> l;

  DeleteInfo(InternalNode<V> gp, InternalNode<V> p, Update pupdate, Leaf<V> l) {
    this.gp = gp;
    this.p = p;
    this.pupdate = pupdate;
    this.l = l;
  }
}