package chaomai.ckdtree;

/**
 * Created by chaomai on 11/10/15.
 */
class DeleteInfo<V> extends Info {
  final InternalNode<V> gp;
  final InternalNode<V> p;
  final Update pupdate;
  final Node<V> sibling;
  final Direction siblingDirection;
  final Leaf<V> l;

  DeleteInfo(InternalNode<V> gp, InternalNode<V> p, Update pupdate, Node<V> sibling,
             Direction siblingDirection, Leaf<V> l) {
    this.gp = gp;
    this.p = p;
    this.pupdate = pupdate;
    this.sibling = sibling;
    this.siblingDirection = siblingDirection;
    this.l = l;
  }
}