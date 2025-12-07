package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.FileReader;
import java.io.IOException;

public class DataLoader {
    public static double[][][] loadAndPrepareData(String fileName) throws IOException {
        JsonArray jsonArray;
        try {
            FileReader reader = new FileReader(fileName);
            jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            System.out.println("JSON soubor úspěšně načten.");
        } catch (JsonSyntaxException e) {
            throw new IOException("Chyba při parsování JSON souboru: " + e.getMessage());
        } catch (IllegalStateException e) {
            throw new IOException("Neplatný formát JSON souboru: " + e.getMessage());
        }

        double[][] inputs = new double[jsonArray.size()][2];
        double[][] outputs = new double[jsonArray.size()][6]; // 3 pro rychlost, 3 pro natočení

        int validCount = 0;
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject obj;
            try {
                obj = jsonArray.get(i).getAsJsonObject();
            } catch (IllegalStateException e) {
                System.err.println("Nepodařilo se načíst JSON objekt na indexu " + i + ": " + e.getMessage());
                continue;
            }

            double temp = obj.get("Teplota").getAsDouble();
            double light = obj.get("Svetlo").getAsDouble();
            int speed = obj.get("Rychlost").getAsInt();
            int rotation = obj.get("Natoceni").getAsInt();

            inputs[validCount][0] = normalize(temp, 13, 40);
            inputs[validCount][1] = normalize(light, 0, 255);

            outputs[validCount][speed] = 1.0;
            outputs[validCount][3 + rotation] = 1.0;
            validCount++;
        }

        double[][] trimmedInputs = new double[validCount][2];
        double[][] trimmedOutputs = new double[validCount][6];
        System.arraycopy(inputs, 0, trimmedInputs, 0, validCount);
        System.arraycopy(outputs, 0, trimmedOutputs, 0, validCount);

        System.out.println("Data úspěšně načtena a připravena.");
        return new double[][][]{trimmedInputs, trimmedOutputs};
    }

    private static double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
}