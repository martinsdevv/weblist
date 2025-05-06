package com.weblist.dao;

import com.weblist.model.Tarefa;
import com.weblist.util.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TarefaDAOImpl implements TarefaDAO {

    @Override
    public void adicionar(Tarefa tarefa) throws SQLException {
        String sql = "INSERT INTO tarefas (titulo, descricao, data_entrega, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tarefa.getTitulo());
            stmt.setString(2, tarefa.getDescricao());
            if (tarefa.getDataEntrega() != null) {
                stmt.setDate(3, Date.valueOf(tarefa.getDataEntrega()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            stmt.setString(4, tarefa.getStatus() != null ? tarefa.getStatus() : "A Fazer"); // Default no BD é 'A Fazer'

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar tarefa, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tarefa.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao criar tarefa, ID não obtido.");
                }
            }
        }
    }

    @Override
    public void atualizar(Tarefa tarefa) throws SQLException {
        String sql = "UPDATE tarefas SET titulo = ?, descricao = ?, data_entrega = ?, status = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tarefa.getTitulo());
            stmt.setString(2, tarefa.getDescricao());
            if (tarefa.getDataEntrega() != null) {
                stmt.setDate(3, Date.valueOf(tarefa.getDataEntrega()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            stmt.setString(4, tarefa.getStatus());
            stmt.setInt(5, tarefa.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void remover(int id) throws SQLException {
        String sql = "DELETE FROM tarefas WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public Tarefa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM tarefas WHERE id = ?";
        Tarefa tarefa = null;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                tarefa = new Tarefa();
                tarefa.setId(rs.getInt("id"));
                tarefa.setTitulo(rs.getString("titulo"));
                tarefa.setDescricao(rs.getString("descricao"));
                Date dataEntregaSql = rs.getDate("data_entrega");
                if (dataEntregaSql != null) {
                    tarefa.setDataEntrega(dataEntregaSql.toLocalDate());
                }
                tarefa.setStatus(rs.getString("status"));
            }
        }
        return tarefa;
    }

    @Override
    public List<Tarefa> listarTodas() throws SQLException {
        List<Tarefa> tarefas = new ArrayList<>();
        String sql = "SELECT * FROM tarefas ORDER BY id ASC"; // ou data_entrega, status, etc.
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Tarefa tarefa = new Tarefa();
                tarefa.setId(rs.getInt("id"));
                tarefa.setTitulo(rs.getString("titulo"));
                tarefa.setDescricao(rs.getString("descricao"));
                Date dataEntregaSql = rs.getDate("data_entrega");
                if (dataEntregaSql != null) {
                    tarefa.setDataEntrega(dataEntregaSql.toLocalDate());
                }
                tarefa.setStatus(rs.getString("status"));
                tarefas.add(tarefa);
            }
        }
        return tarefas;
    }
} 