package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class SearchRes<V> {
  static final Object RESTART = new Object();
  InternalNode<V> gp;
  Update gpupdate;
  InternalNode<V> p;
  Update pupdate;
  Leaf<V> l;
  int leafDepth;

  SearchRes(InternalNode<V> gp, Update gpupdate, InternalNode<V> p, Update pupdate, Leaf<V> l,
            int leafDepth) {
    this.gp = gp;
    this.gpupdate = gpupdate;
    this.p = p;
    this.pupdate = pupdate;
    this.l = l;
    this.leafDepth = leafDepth;
  }
}
