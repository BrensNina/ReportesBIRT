package com.flotasys.reportes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class TestConexion {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://127.0.0.1:5432/flotasys", "postgres", "1234")) {
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("SELECT count(*) FROM \"CostoOperacionMensual\"");
            rs.next();
            System.out.println("Conexion OK, filas en CostoOperacionMensual: " + rs.getInt(1));
        }
    }
}
