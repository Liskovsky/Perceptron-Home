package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import com.fazecast.jSerialComm.SerialPort;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.nd4j.linalg.ops.transforms.Transforms.softmax;

public class MicrobitController {
    private static MultiLayerNetwork model;
    private static SerialPort serialPort;
    private static double temp = 0.0;
    private static double light = 0.0;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    public MicrobitController() {
        try {
            model = MultiLayerNetwork.load(new File("trained_model.zip"), false);
            if (model != null) {
                System.out.println("Model úspěšně načten.");
            } else {
                System.err.println("Model nebyl načten.");
            }
        } catch (IOException e) {
            System.err.println("Chyba při načítání modelu: " + e.getMessage());
        }

        // Inicializace sériového portu
        String portName = "COM3";
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(115200);

        if (serialPort.openPort()) {
            System.out.println("Sériový port otevřen.");
        } else {
            System.err.println("Chyba při otevírání sériového portu.");
        }
    }

    public void startPeriodicChecking() {
        executorService.scheduleAtFixedRate(this::predictAndDisplayCommands, 0, 30, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(() -> {
            try {
                processSensorData();
            } catch (IOException e) {
                System.err.println("Chyba při čtení dat: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void processSensorData() throws IOException {
        byte[] readBuffer = new byte[1024];
        int bytesRead = serialPort.readBytes(readBuffer, readBuffer.length);

        if (bytesRead > 0) {
            String receivedData = new String(readBuffer, 0, bytesRead).trim();
            int start = receivedData.indexOf("{");
            int end = receivedData.indexOf("}", start);

            if (start != -1 && end != -1) {
                String jsonString = receivedData.substring(start, end + 1);
                try {
                    JsonObject jsonData = JsonParser.parseString(jsonString).getAsJsonObject();
                    temp = jsonData.get("Teplota").getAsDouble();
                    light = jsonData.get("Svetlo").getAsDouble();
                    System.out.println("Přijatá data: Teplota: " + temp + ", Světlo: " + light);

                    // Zobrazení přijatých dat
                    System.out.println("Přijatá data: Teplota: " + temp + ", Světlo: " + light);

                } catch (Exception e) {
                    System.err.println("Chyba při parsování JSON: " + e.getMessage());
                }
            }
        }
    }

    private void predictAndDisplayCommands() {
        INDArray input = Nd4j.create(new double[][]{{temp, light}});
        System.out.println("Vstupní data pro model: Teplota: " + temp + ", Světlo: " + light);

        // Provádění predikce
        INDArray output = model.output(input);

        System.out.println("Výstup modelu (skóre): " + output);

        // Aplikace softmax funkce na výstupy pro ventilátor a žaluzie
        INDArray fanOutput = output.get(NDArrayIndex.all(), NDArrayIndex.interval(0, 3));
        INDArray blindsOutput = output.get(NDArrayIndex.all(), NDArrayIndex.interval(3, 6));

        INDArray fanProbabilities = softmax(fanOutput);
        INDArray blindsProbabilities = softmax(blindsOutput);

        // Extrakce predikovaných tříd
        int fanSpeedClass = fanProbabilities.argMax(1).getInt(0);
        int blindsPositionClass = blindsProbabilities.argMax(1).getInt(0);

        // Výpis predikovaných tříd na obrazovku
        System.out.println("Predikovaná rychlost ventilátoru: " + fanSpeedClass +
                ", Pozice žaluzií: " + blindsPositionClass);

        // Sestavení zprávy ve formátu "rychlost ventilátoru,pozice žaluzií"
        String message = fanSpeedClass + "," + blindsPositionClass + "\n";
        serialPort.writeBytes(message.getBytes(), message.length());

        System.out.println("Odeslaná zpráva: " + message);
    }
}
