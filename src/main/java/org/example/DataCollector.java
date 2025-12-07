package org.example;

import com.fazecast.jSerialComm.SerialPort;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataCollector {
    private final SerialPort comPort;
    private final File file;
    private final StringBuilder dataBuffer = new StringBuilder();

    public DataCollector(String portName, String fileName) {
        comPort = SerialPort.getCommPort(portName);
        comPort.setBaudRate(115200);
        file = new File(fileName);
    }

    public void startCollecting() {
        comPort.openPort();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write("[\n");

            comPort.addDataListener(new com.fazecast.jSerialComm.SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        return;
                    }

                    try {
                        byte[] readBuffer = new byte[1024];
                        int bytesRead = comPort.readBytes(readBuffer, readBuffer.length);

                        if (bytesRead > 0) {
                            String receivedData = new String(readBuffer, 0, bytesRead).trim();
                            dataBuffer.append(receivedData);

                            String dataString = dataBuffer.toString();
                            int start = dataString.indexOf('$');
                            int end = dataString.indexOf('$', start + 1);

                            if (start != -1 && end != -1) {
                                String jsonString = dataString.substring(start + 1, end);
                                try {
                                    JsonObject jsonData = JsonParser.parseString(jsonString).getAsJsonObject();
                                    writer.write(jsonData.toString() + ",\n");
                                    writer.flush();
                                    dataBuffer.delete(0, end + 1);
                                } catch (Exception e) {
                                    System.err.println("Chyba při parsování JSON: " + e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Chyba při zpracování dat: " + e.getMessage());
                    }
                }
            });

            System.out.println("Data logger běží. Stiskněte Ctrl+C pro ukončení.");
            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            System.err.println("Chyba: " + e.getMessage());
        } finally {
            comPort.closePort();
        }
    }
}
