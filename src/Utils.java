import java.util.*;

public class Utils {

    public static boolean contains(int[] arr, int val) {
        return arr == null ? false : Arrays.stream(arr).anyMatch(i -> i == val);
    }

    public static int[] add(int[] arr, int newVal) {
        if (arr == null) {
            int[] newArr = { newVal };
            return newArr;
        } else {
            int[] newArr = Arrays.copyOf(arr, arr.length + 1);
            newArr[arr.length] = newVal;
            return newArr;
        }
    }

    public static int[] removeElement(int[] arr, int deleteVal) {
        if (arr == null) {
            return null;
        }
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == deleteVal) {
                int[] copy = new int[arr.length - 1];
                System.arraycopy(arr, 0, copy, 0, i);
                System.arraycopy(arr, i + 1, copy, i, arr.length - i - 1);
                return copy;
            }
        }
        return arr;
    }
}
