package chaomai.ckdtree;

/**
 * Created by chaomai on 11/10/15.
 */
class DeleteInfo<V> extends Info {
  InternalNode<V> gp;
  InternalNode<V> p;
  Node<V> sibling;
  Direction siblingDirection;
  Leaf<V> l;
  Update pupdate;

  DeleteInfo(InternalNode<V> gp, InternalNode<V> p, Node<V> sibling, Direction siblingDirection,
             Leaf<V> l, Update pupdate) {
    this.gp = gp;
    this.p = p;
    this.sibling = sibling;
    this.siblingDirection = siblingDirection;
    this.l = l;
    this.pupdate = pupdate;
  }
}