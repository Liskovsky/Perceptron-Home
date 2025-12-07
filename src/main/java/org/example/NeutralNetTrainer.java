package org.example;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;

public class NeutralNetTrainer {
    private static final int NUM_EPOCHS = 200;
    private static final int NUM_TEST_SAMPLES = 20; // Počet testovacích záznamů
    private static final int EARLY_STOPPING_PATIENCE = 10;  // Počet epoch bez zlepšení, než dojde k zastavení

    public void trainNeuralNetwork() {
        try {
            // Načtení a příprava dat
            double[][][] preparedData = loadAndPrepareData();
            DataProcessing.SplitData splitData = DataProcessing.splitData(preparedData[0], preparedData[1], NUM_TEST_SAMPLES);

            // Vytvoření DataSet objektů
            DataSet trainDataSet = new DataSet(Nd4j.create(splitData.trainInputs), Nd4j.create(splitData.trainOutputs));
            DataSet testDataSet = new DataSet(Nd4j.create(splitData.testInputs), Nd4j.create(splitData.testOutputs));

            MultiLayerNetwork model = new ModelUtils().createModel();

            // Trénování modelu s metodou Early Stopping
            trainModel(model, trainDataSet);

            // Vyhodnocení a uložení modelu
            evaluateModel(model, testDataSet);
            saveTrainedModel(model);

        } catch (IOException e) {
            System.err.println("Chyba při načítání nebo zpracování dat: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Neočekávaná chyba: " + e.getMessage());
        }
    }

    private double[][][] loadAndPrepareData() throws IOException {
        return DataLoader.loadAndPrepareData("microbit_data.json");
    }

    private void trainModel(MultiLayerNetwork model, DataSet trainDataSet) {
        double bestLoss = Double.MAX_VALUE;
        int epochsWithoutImprovement = 0;

        for (int epoch = 0; epoch < NUM_EPOCHS; epoch++) {
            model.fit(trainDataSet);
            double currentLoss = model.score();

            System.out.println("Epoch: " + epoch + ", Loss: " + currentLoss);

            // Early Stopping: Zastaví trénink, pokud se ztráta nezlepšuje
            if (currentLoss < bestLoss) {
                bestLoss = currentLoss;
                epochsWithoutImprovement = 0;
            } else {
                epochsWithoutImprovement++;
            }

            if (epochsWithoutImprovement >= EARLY_STOPPING_PATIENCE) {
                System.out.println("Early stopping: Zastavení trénování po " + epoch + " epochách.");
                break;
            }
        }
    }

    private void evaluateModel(MultiLayerNetwork model, DataSet testDataSet) {
        // Vyhodnocení přesnosti modelu na testovacích datech
        INDArray predictions = model.output(testDataSet.getFeatures());
        PredictionEvaluator.evaluatePredictions(predictions, testDataSet.getLabels());
    }

    private void saveTrainedModel(MultiLayerNetwork model) {
        try {
            ModelUtils.saveModel(model, "trained_model.zip");
            System.out.println("Model byl úspěšně uložen na: " + "trained_model.zip");
        } catch (IOException e) {
            System.err.println("Chyba při ukládání modelu: " + e.getMessage());
        }
    }
}