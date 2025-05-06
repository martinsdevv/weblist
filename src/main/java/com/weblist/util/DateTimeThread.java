package com.weblist.util;

import javafx.application.Platform;
import javafx.scene.control.Label;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeThread implements Runnable {

    private final Label dateTimeLabel;
    private volatile boolean running = true;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public DateTimeThread(Label dateTimeLabel) {
        this.dateTimeLabel = dateTimeLabel;
    }

    @Override
    public void run() {
        while (running) {
            Platform.runLater(() -> {
                dateTimeLabel.setText(LocalDateTime.now().format(formatter));
            });
            try {
                Thread.sleep(1000); // Atualiza a cada segundo
            } catch (InterruptedException e) {
                // Thread interrompida, pode ser durante o fechamento da aplicação
                running = false; // Garante que o loop termine
                Thread.currentThread().interrupt(); // Restaura o status de interrupção
            }
        }
    }

    public void stop() {
        running = false;
    }
} 