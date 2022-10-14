package com.yube.sorters;

public class MergeSorter {

    private static final int SIZE_THRESHOLD = 16;

    public static void sort(Comparable[] a) {
        sort(a, 0, a.length - 1);
    }

    public static Comparable[] merge(Comparable[] left, Comparable[] right) {

        int leftLength = left.length;
        int rightLength = right.length;

        Comparable[] merged = new Comparable[leftLength + rightLength];

        int leftPosition, rightPosition, mergedPosition;
        leftPosition = rightPosition = mergedPosition = 0;

        while (leftPosition < leftLength && rightPosition < rightLength) {
            if (left[leftPosition].compareTo(right[rightPosition]) < 0) {
                merged[mergedPosition++] = left[leftPosition++];
            } else {
                merged[mergedPosition++] = right[rightPosition++];
            }
        }

        while (leftPosition < leftLength) {
            merged[mergedPosition++] = left[leftPosition++];
        }

        while (rightPosition < rightLength) {
            merged[mergedPosition++] = right[rightPosition++];
        }

        return merged;
    }

    private static void sort(Comparable[] a, int lo, int hi) {
        if (hi - lo < SIZE_THRESHOLD) {
            insertionSort(a, lo, hi);
            return;
        }

        Comparable[] tmp = new Comparable[((hi - lo) / 2) + 1];
        mergeSort(a, tmp, lo, hi);
    }

    private static void mergeSort(Comparable[] a, Comparable[] tmp, int lo, int hi) {
        if (hi - lo < SIZE_THRESHOLD) {
            insertionSort(a, lo, hi);
            return;
        }

        int m = (lo + hi) / 2;
        mergeSort(a, tmp, lo, m);
        mergeSort(a, tmp, m + 1, hi);
        merge(a, tmp, lo, m, hi);
    }

    private static void merge(Comparable[] a, Comparable[] b, int lo, int m, int hi) {
        if (a[m].compareTo(a[m + 1]) <= 0)
            return;

        System.arraycopy(a, lo, b, 0, m - lo + 1);

        int i = 0;
        int j = m + 1;
        int k = lo;

        // copy back next-greatest element at each time
        while (k < j && j <= hi) {
            if (b[i].compareTo(a[j]) <= 0) {
                a[k++] = b[i++];
            } else {
                a[k++] = a[j++];
            }
        }

        // copy back remaining elements of first half (if any)
        System.arraycopy(b, i, a, k, j - k);

    }

    private static void insertionSort(Comparable[] a, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            int j = i;
            Comparable t = a[j];
            while (j > lo && t.compareTo(a[j - 1]) < 0) {
                a[j] = a[j - 1];
                --j;
            }
            a[j] = t;
        }
    }
}