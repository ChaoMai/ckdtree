package chaomai.ckdtree.snapshot2;

/**
 * Created by chaomai on 11/22/15.
 */
class SearchRes<V> {
  final InternalNode gp;
  final Update gpupdate;
  final InternalNode p;
  final Update pupdate;
  final Leaf<V> l;
  final int leafDepth;

  SearchRes(InternalNode gp, Update gpupdate, InternalNode p, Update pupdate, Leaf<V> l,
            int leafDepth) {
    this.gp = gp;
    this.gpupdate = gpupdate;
    this.p = p;
    this.pupdate = pupdate;
    this.l = l;
    this.leafDepth = leafDepth;
  }
}