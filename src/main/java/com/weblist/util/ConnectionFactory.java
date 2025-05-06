package com.weblist.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    // Configure com seus dados do PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/weblist";
    private static final String USER = "postgres";
    private static final String PASSWORD = "powderzero";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Em uma aplicação real, trate essa exceção de forma mais robusta
            // (logs, mensagem para o usuário, etc.)
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace(); // Apenas para depuração inicial
            throw new RuntimeException("Erro na conexão com o banco de dados", e);
        }
    }

    // Opcional: Método para fechar conexões, statements e resultsets
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
    // Adicione métodos para fechar Statement e ResultSet se necessário
} 