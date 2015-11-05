package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class Update {
  State state;
  Info info;
  int depth;

  Update() {
    this.state = State.CLEAN;
    this.info = null;
    this.depth = 0;
  }

  Update(State state, Info info, int depth) {
    this.state = state;
    this.info = info;
    this.depth = depth;
  }
}