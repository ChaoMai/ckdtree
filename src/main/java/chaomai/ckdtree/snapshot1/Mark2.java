package chaomai.ckdtree.snapshot1;

/**
 * Created by chaomai on 12/2/15.
 */
class Mark2<V> extends Info {
  final DeleteInfo<V> deleteInfo;

  Mark2(DeleteInfo<V> deleteInfo) {
    this.deleteInfo = deleteInfo;
  }
}