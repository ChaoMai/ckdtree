package chaomai.ckdtree.snapshot1;

/**
 * Created by chaomai on 12/2/15.
 */
class Mark1<V> extends Info {
  final DeleteInfo<V> deleteInfo;

  Mark1(DeleteInfo<V> deleteInfo) {
    this.deleteInfo = deleteInfo;
  }
}