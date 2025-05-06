package com.weblist.dao;

import com.weblist.model.Tarefa;
import java.sql.SQLException;
import java.util.List;

public interface TarefaDAO {
    void adicionar(Tarefa tarefa) throws SQLException;
    void atualizar(Tarefa tarefa) throws SQLException;
    void remover(int id) throws SQLException;
    Tarefa buscarPorId(int id) throws SQLException;
    List<Tarefa> listarTodas() throws SQLException;
} 