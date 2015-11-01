package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class SearchRes<V> {
  InternalNode gp;
  Update gpupdate;
  InternalNode p;
  Update pupdate;
  Leaf<V> l;
  int leafDepth;

  static final Object RESTART = new Object();

  SearchRes(InternalNode gp, Update gpupdate, InternalNode p, Update pupdate, Leaf<V> l, int leafDepth) {
    this.gp = gp;
    this.gpupdate = gpupdate;
    this.p = p;
    this.pupdate = pupdate;
    this.l = l;
    this.leafDepth = leafDepth;
  }
}
