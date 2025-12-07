package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataProcessing {

    public static class SplitData {
        public double[][] trainInputs;
        public double[][] trainOutputs;
        public double[][] testInputs;
        public double[][] testOutputs;

        public SplitData(double[][] trainInputs, double[][] trainOutputs, double[][] testInputs, double[][] testOutputs) {
            this.trainInputs = trainInputs;
            this.trainOutputs = trainOutputs;
            this.testInputs = testInputs;
            this.testOutputs = testOutputs;
        }
    }

    public static SplitData splitData(double[][] inputs, double[][] outputs, int numTestSamples) {
        if (numTestSamples >= inputs.length) {
            throw new IllegalArgumentException("Počet testovacích záznamů musí být menší než celkový počet dat.");
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < inputs.length; i++) indices.add(i);
        Collections.shuffle(indices);

        int trainSize = inputs.length - numTestSamples;

        double[][] trainInputs = new double[trainSize][];
        double[][] trainOutputs = new double[trainSize][];
        double[][] testInputs = new double[numTestSamples][];
        double[][] testOutputs = new double[numTestSamples][];

        for (int i = 0; i < indices.size(); i++) {
            if (i < trainSize) {
                trainInputs[i] = inputs[indices.get(i)];
                trainOutputs[i] = outputs[indices.get(i)];
            } else {
                testInputs[i - trainSize] = inputs[indices.get(i)];
                testOutputs[i - trainSize] = outputs[indices.get(i)];
            }
        }

        return new SplitData(trainInputs, trainOutputs, testInputs, testOutputs);
    }
}