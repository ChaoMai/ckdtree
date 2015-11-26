package chaomai.ckdtree;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by chaomai on 11/5/15.
 */
public class Utilities {
  public static double[] generateRandomArray(int dimension) {
    Random random = new Random(System.nanoTime());
    double[] array = new double[dimension];
    for (int i = 0; i < dimension; ++i) {
      array[i] = random.nextDouble();
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

  public static String KeyToString(double[] key) {
    String ret = new String();
    for (double d : key) {
      ret += d + ",";
    }

    return ret;
  }

  public static void printEntry(Map.Entry<double[], Integer> l) {
    System.out.println(String.format("<[%s], %d>", KeyToString(l.getKey()), l.getValue()));
  }

  public static int makeDuplicateKeys(double[][] array) {
    Random rand = new Random();
    int min = 0;
    int max = array.length - 1;

    // pickup a random index
    int randIndex = rand.nextInt((max - min) + 1) + min;

    // choose a random length of keys
    int randLength;
    while ((randLength = rand.nextInt((max - min) + 1) + min) >= max - randIndex) {
      continue;
    }

    for (int i = randIndex + 1; i < randIndex + randLength; ++i) {
      array[i] = array[randIndex];
    }

    return randLength - 1;
  }

  public static void stopExecutor(ExecutorService executor) {
    try {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}