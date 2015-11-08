package chaomai.ckdtree;

/**
 * Created by chaomai on 11/5/15.
 */
class Utilties {
  public static double[] generateRandomArray(int dimension) {
    double[] array = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      array[i] = Math.random() * 10;
    }
    return array;
  }

  public static double[][] generateRandomArrays(int samples, int dimension) {
    double[][] array = new double[samples][dimension];
    for (int i = 0; i < samples; ++i) {
      array[i] = generateRandomArray(dimension);
    }
    return array;
  }

  public static void printArray(double[] array) {
    String str = "[";

    for (double d : array) {
      str += d + ", ";
    }
    str = str.substring(0, str.length() - 2);
    str += "]";

    System.out.println(str);
  }

  public static void printArray(double[][] array) {
    for (double[] arr : array) {
      printArray(arr);
    }
  }
}