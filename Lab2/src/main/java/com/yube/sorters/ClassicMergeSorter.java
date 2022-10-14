package com.yube.sorters;

public class ClassicMergeSorter implements Sorter {

    @Override
    public String getAlgorithmName() {
        return "Classic Merge Sort";
    }

    @Override
    public void sort(Comparable[] array) {
        MergeSorter.sort(array);
    }
}