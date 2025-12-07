package org.example;

public class Main {
    public static void main(String[] args) {
        // Trénink neuronové sítě s existujícími daty
        NeutralNetTrainer trainer = new NeutralNetTrainer();
        trainer.trainNeuralNetwork();

        // Spuštění kontroly a odesílání příkazů každých 30 sekund
        MicrobitController controller = new MicrobitController();
        controller.startPeriodicChecking();
    }
}