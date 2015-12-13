package chaomai.ckdtree.snapshot1;

/**
 * Created by chaomai on 12/9/15.
 */
class RDCSSDescriptor<V> {
  final Node<V> or;
  final Node<V> ol;
  final Node<V> nr;
  volatile boolean committed;

  RDCSSDescriptor(final Node<V> or, final Node<V> ol, final Node<V> nr) {
    this.or = or;
    this.ol = ol;
    this.nr = nr;
    this.committed = false;
  }
}