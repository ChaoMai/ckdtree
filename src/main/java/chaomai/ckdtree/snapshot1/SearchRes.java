package chaomai.ckdtree.snapshot1;

/**
 * Created by chaomai on 12/2/15.
 */
class SearchRes<V> {
  static final Object RESTART = new Object();
  final Node<V> gp;
  final Info gpinfo;
  final Node<V> p;
  final Info pinfo;
  final Node<V> l;
  final int leafDepth;

  SearchRes(Node<V> gp, Info gpinfo, Node<V> p, Info pinfo, Node<V> l, int leafDepth) {
    this.gp = gp;
    this.gpinfo = gpinfo;
    this.p = p;
    this.pinfo = pinfo;
    this.l = l;
    this.leafDepth = leafDepth;
  }
}