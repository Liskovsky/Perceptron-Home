package org.example;

import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;

public class PredictionEvaluator {

    public static void evaluatePredictions(INDArray predictions, INDArray actuals) {
        Evaluation eval = new Evaluation();
        eval.eval(actuals, predictions);

        System.out.println("Celková přesnost: " + eval.accuracy());
        System.out.println();
        System.out.println("Testovací výstupy:");

        int correctFanSpeedPredictions = 0;
        int correctBlindsPositionPredictions = 0;

        for (int i = 0; i < predictions.rows(); i++) {
            double[] predArray = predictions.getRow(i).toDoubleVector();
            double[] actualArray = actuals.getRow(i).toDoubleVector();

            int fanSpeedClass = getClassFromPrediction(predArray, 0, 3);
            int blindsPositionClass = getClassFromPrediction(predArray, 3, 6);

            int actualFanSpeedClass = getClassFromActual(actualArray, 0, 3);
            int actualBlindsPositionClass = getClassFromActual(actualArray, 3, 6);

            System.out.println("Predikce: Ventilátor = " + fanSpeedClass +
                    ", Žaluzie = " + blindsPositionClass);
            System.out.println("Realita: Ventilátor = " + actualFanSpeedClass +
                    ", Žaluzie = " + actualBlindsPositionClass);

            if (fanSpeedClass == actualFanSpeedClass) {
                correctFanSpeedPredictions++;
            }
            if (blindsPositionClass == actualBlindsPositionClass) {
                correctBlindsPositionPredictions++;
            }
        }

        double fanSpeedAccuracy = (double) correctFanSpeedPredictions / predictions.rows();
        double blindsPositionAccuracy = (double) correctBlindsPositionPredictions / predictions.rows();

        System.out.println("Přesnost predikce rychlosti větráku: " + fanSpeedAccuracy);
        System.out.println("Přesnost predikce pozice žaluzií: " + blindsPositionAccuracy);
    }

    private static int getClassFromPrediction(double[] predArray, int startIdx, int endIdx) {
        int maxIdx = startIdx;
        double maxVal = predArray[startIdx];
        for (int i = startIdx + 1; i < endIdx; i++) {
            if (predArray[i] > maxVal) {
                maxVal = predArray[i];
                maxIdx = i;
            }
        }
        return maxIdx - startIdx;
    }

    private static int getClassFromActual(double[] actualArray, int startIdx, int endIdx) {
        for (int i = startIdx; i < endIdx; i++) {
            if (actualArray[i] == 1.0) {
                return i - startIdx;
            }
        }
        return -1;
    }
}