package chaomai.ckdtree.snapshot3;

/**
 * Created by chaomai on 12/2/15.
 */
class DeleteInfo<V> extends Info {
  final Node<V> gp;
  final Node<V> p;
  final Info pinfo;
  final Node<V> l;

  DeleteInfo(Node<V> gp, Node<V> p, Info pinfo, Node<V> l) {
    this.gp = gp;
    this.p = p;
    this.pinfo = pinfo;
    this.l = l;
  }
}