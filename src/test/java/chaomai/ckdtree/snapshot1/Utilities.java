package chaomai.ckdtree.snapshot1;

import java.util.Random;

/**
 * Created by chaomai on 11/5/15.
 */
public class Utilities {
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
}