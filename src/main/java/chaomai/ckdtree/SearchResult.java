package chaomai.ckdtree;

class SearchResult<TData> {
  public final InternalNode grandparent;
  //public final Node grandparent;
  public final Flag grandparentVersion;
  public final InternalNode parent;
  //public final Node parent;
  public final Flag parentVersion;
  public final Leaf<TData> leaf;
  //public final Node leaf;
  public final int leafDepth;

  public SearchResult(InternalNode gp, InternalNode p, Leaf<TData> l, int ldepth, Flag gpVersion, Flag pVersion) {
    grandparent = gp;
    parent = p;
    leaf = l;
    leafDepth = ldepth;
    grandparentVersion = gpVersion;
    parentVersion = pVersion;
  }
  //	public SearchResult(Node gp, Node p,
  //			Node l, int ldepth, Version gpVersion,
  //			Version pVersion) {
  //		grandparent = gp;
  //		parent = p;
  //		leaf = l;
  //		leafDepth = ldepth;
  //		grandparentVersion = gpVersion;
  //		parentVersion = pVersion;
  //	}

}

