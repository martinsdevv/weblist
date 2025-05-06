package com.weblist.controller;

import com.weblist.dao.TarefaDAO;
import com.weblist.dao.TarefaDAOImpl;
import com.weblist.model.Tarefa;
import com.weblist.util.DateTimeThread;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;
import javafx.scene.layout.ColumnConstraints;
import javafx.stage.Window;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TaskListController {

    @FXML private Label lblDateTime;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtDescricao;
    @FXML private DatePicker datePickerEntrega;
    @FXML private ComboBox<String> comboStatus;
    
    @FXML private ListView<Tarefa> listViewTarefas;

    @FXML private Button btnAdicionar;
    @FXML private Button btnAtualizar;
    @FXML private Button btnLimparCampos;

    private TarefaDAO tarefaDAO;
    private ObservableList<Tarefa> observableListTarefas;
    private DateTimeThread dateTimeThread;
    private Tarefa tarefaSelecionadaParaEdicao = null;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TaskListController() {
        tarefaDAO = new TarefaDAOImpl();
    }

    @FXML
    public void initialize() {
        comboStatus.setItems(FXCollections.observableArrayList("A Fazer", "Em Andamento", "Concluído"));
        comboStatus.setValue("A Fazer");

        listViewTarefas.setCellFactory(param -> new ListCell<Tarefa>() {
            private final GridPane gridPane = new GridPane();
            private final Label tituloLabel = new Label();
            private final Label descricaoLabel = new Label();
            private final Label dataEntregaLabel = new Label();
            private final Label statusLabel = new Label();

            { // Bloco de inicialização para a célula
                tituloLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                descricaoLabel.setWrapText(true); // Permite quebra de linha na descrição

                gridPane.setHgap(10);
                gridPane.setVgap(5);
                gridPane.setPadding(new Insets(5, 10, 5, 10));

                // Coluna 0 para Título e Descrição, Coluna 1 para Data e Status
                ColumnConstraints col1 = new ColumnConstraints();
                col1.setHgrow(Priority.ALWAYS);
                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPrefWidth(150);

                gridPane.getColumnConstraints().addAll(col1, col2);

                // Adicionando labels ao GridPane
                // Linha 0
                gridPane.add(tituloLabel, 0, 0);
                gridPane.add(statusLabel, 1, 0);
                // Linha 1
                gridPane.add(descricaoLabel, 0, 1);
                GridPane.setColumnSpan(descricaoLabel, 2);
                // Linha 2
                gridPane.add(dataEntregaLabel, 0, 2);


            }

            @Override
            protected void updateItem(Tarefa tarefa, boolean empty) {
                super.updateItem(tarefa, empty);
                if (empty || tarefa == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    tituloLabel.setText(tarefa.getTitulo());
                    
                    String descricaoText = (tarefa.getDescricao() == null || tarefa.getDescricao().trim().isEmpty()) ? "Sem descrição" : tarefa.getDescricao();
                    descricaoLabel.setText(descricaoText); // Não precisa do "Descrição: " se estiver em layout grid

                    String dataEntregaText = (tarefa.getDataEntrega() == null) ? "Não definida" : tarefa.getDataEntrega().format(dateFormatter);
                    dataEntregaLabel.setText("Entrega: " + dataEntregaText);
                    
                    statusLabel.setText("Status: " + tarefa.getStatus());
                    if ("Concluído".equals(tarefa.getStatus())) {
                        statusLabel.setStyle("-fx-text-fill: green;");
                    } else if ("Em Andamento".equals(tarefa.getStatus())) {
                        statusLabel.setStyle("-fx-text-fill: orange;");
                    } else {
                        statusLabel.setStyle("-fx-text-fill: black;");
                    }    

                    setGraphic(gridPane);
                }
            }
        });
        
        carregarTarefasNaLista();

        dateTimeThread = new DateTimeThread(lblDateTime);
        Thread thread = new Thread(dateTimeThread);
        thread.setDaemon(true);
        thread.start();

        btnAtualizar.setVisible(false);
        btnAtualizar.setManaged(false);
    }

    private void carregarTarefasNaLista() {
        try {
            List<Tarefa> tarefas = tarefaDAO.listarTodas();
            observableListTarefas = FXCollections.observableArrayList(tarefas);
            listViewTarefas.setItems(observableListTarefas);
        } catch (SQLException e) {
            mostrarAlertaErro("Erro ao carregar tarefas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdicionarTarefa() {
        String titulo = txtTitulo.getText();
        String descricao = txtDescricao.getText();
        LocalDate dataEntrega = datePickerEntrega.getValue();
        String status = comboStatus.getValue();

        if (titulo == null || titulo.trim().isEmpty()) {
            mostrarAlertaErro("O título é obrigatório.");
            return;
        }
        
        Tarefa novaTarefa = new Tarefa(0, titulo, descricao, dataEntrega, status);
        try {
            tarefaDAO.adicionar(novaTarefa);
            carregarTarefasNaLista(); 
            limparCamposDeEntrada();
        } catch (SQLException e) {
            mostrarAlertaErro("Erro ao adicionar tarefa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAtualizarTarefa() { 
        if (tarefaSelecionadaParaEdicao == null) {
            Tarefa selecionada = listViewTarefas.getSelectionModel().getSelectedItem();
            if(selecionada == null) {
                 mostrarAlertaErro("Nenhuma tarefa selecionada para atualizar.");
                 return;
            }
            tarefaSelecionadaParaEdicao = selecionada;
        }

        String titulo = txtTitulo.getText();
        String descricao = txtDescricao.getText();
        LocalDate dataEntrega = datePickerEntrega.getValue();
        String status = comboStatus.getValue();

        if (titulo == null || titulo.trim().isEmpty()) {
            mostrarAlertaErro("O título é obrigatório.");
            return;
        }

        tarefaSelecionadaParaEdicao.setTitulo(titulo);
        tarefaSelecionadaParaEdicao.setDescricao(descricao);
        tarefaSelecionadaParaEdicao.setDataEntrega(dataEntrega);
        tarefaSelecionadaParaEdicao.setStatus(status);

        try {
            tarefaDAO.atualizar(tarefaSelecionadaParaEdicao);
            carregarTarefasNaLista();
            limparCamposDeEntrada();
            modoInsercao(); 
        } catch (SQLException e) {
            mostrarAlertaErro("Erro ao atualizar tarefa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditarTarefa() {
        Tarefa selecionada = listViewTarefas.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            tarefaSelecionadaParaEdicao = selecionada;
            txtTitulo.setText(selecionada.getTitulo());
            txtDescricao.setText(selecionada.getDescricao());
            datePickerEntrega.setValue(selecionada.getDataEntrega());
            comboStatus.setValue(selecionada.getStatus());
            modoEdicao();
        } else {
            mostrarAlertaErro("Selecione uma tarefa para editar.");
        }
    }

    @FXML
    private void handleExcluirTarefa() {
        Tarefa selecionada = listViewTarefas.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText("Excluir Tarefa: " + selecionada.getTitulo());
            alert.setContentText("Você tem certeza que deseja excluir esta tarefa?");

            Window ownerWindow = listViewTarefas.getScene().getWindow();
            alert.initOwner(ownerWindow);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    tarefaDAO.remover(selecionada.getId());
                    carregarTarefasNaLista(); 
                } catch (SQLException e) {
                    mostrarAlertaErro("Erro ao excluir tarefa: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            mostrarAlertaErro("Selecione uma tarefa para excluir.");
        }
    }

    @FXML
    private void handleLimparCampos() {
        limparCamposDeEntrada();
        listViewTarefas.getSelectionModel().clearSelection();
        if (tarefaSelecionadaParaEdicao != null) { 
            modoInsercao();
        }
    }

    private void limparCamposDeEntrada() {
        txtTitulo.clear();
        txtDescricao.clear();
        datePickerEntrega.setValue(null);
        comboStatus.setValue("A Fazer"); 
        tarefaSelecionadaParaEdicao = null;
        listViewTarefas.getSelectionModel().clearSelection();
    }

    private void modoEdicao() {
        btnAdicionar.setVisible(false);
        btnAdicionar.setManaged(false);
        btnAtualizar.setVisible(true);
        btnAtualizar.setManaged(true);
    }

    private void modoInsercao() {
        btnAdicionar.setVisible(true);
        btnAdicionar.setManaged(true);
        btnAtualizar.setVisible(false);
        btnAtualizar.setManaged(false);
        tarefaSelecionadaParaEdicao = null;
    }

    private void mostrarAlertaErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        if (listViewTarefas != null && listViewTarefas.getScene() != null) {
            Window ownerWindow = listViewTarefas.getScene().getWindow();
            alert.initOwner(ownerWindow);
        } else {
            System.err.println("Não foi possível definir o owner para o Alert, a cena não está pronta.");
        }

        alert.showAndWait();
    }

    public void shutdown() {
        if (dateTimeThread != null) {
            dateTimeThread.stop();
        }
    }
} 