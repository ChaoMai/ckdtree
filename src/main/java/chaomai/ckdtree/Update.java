package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class Update {
  final State state;
  final Info info;

  Update() {
    this.state = State.CLEAN;
    this.info = null;
  }

  Update(State state, Info info) {
    this.state = state;
    this.info = info;
  }

  @Override
  public String toString() {
    return "state: " + this.state;
  }
}