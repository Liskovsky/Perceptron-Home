package org.example;

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.BatchNormalization;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

public class ModelUtils {

    private static final int NUM_INPUTS = 2;  // Počet vstupů: Teplota a Světlo
    private static final int NUM_OUTPUTS = 6; // Počet výstupů: 3 rychlosti a 3 natočení

    public MultiLayerNetwork createModel() {
        // Konfigurace neuronové sítě s L2 regularizací a adaptivním optimalizátorem Adam
        return new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(123)
                .l2(0.0001) // L2 regularizace
                .updater(Updater.ADAM) // Použití Adam optimalizátoru
                .weightInit(WeightInit.XAVIER) // Inicializace vah pomocí Xavier metody
                .list()
                .layer(0, new DenseLayer.Builder().nIn(NUM_INPUTS).nOut(64)
                        .activation(Activation.RELU)  // Aktivace RELU
                        .build())
                .layer(new BatchNormalization.Builder().build())
                .layer(1, new DenseLayer.Builder().nIn(64).nOut(128)
                        .activation(Activation.RELU)  // Aktivace RELU
                        .dropOut(0.5)
                        .build())
                .layer(new BatchNormalization.Builder().build())
                .layer(2, new DenseLayer.Builder().nIn(128).nOut(256)
                        .activation(Activation.RELU)  // Aktivace RELU
                        .dropOut(0.5)
                        .build())
                .layer(new BatchNormalization.Builder().build())
                .layer(3, new OutputLayer.Builder().nIn(256).nOut(NUM_OUTPUTS)
                        .activation(Activation.IDENTITY)  // pro regresní výstupy
                        .lossFunction(LossFunctions.LossFunction.MSE) // Funkce ztráty
                        .build())
                .build());
    }

    public static void saveModel(MultiLayerNetwork model, String filePath) throws IOException {
        model.save(new File(filePath));
    }

    public static MultiLayerNetwork loadModel(String filePath) throws IOException {
        return MultiLayerNetwork.load(new File(filePath), false);
    }
}