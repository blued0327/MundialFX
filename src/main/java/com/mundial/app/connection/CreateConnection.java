

package com.mundial.app.connection;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateConnection {

    // SINGLETON - parte 1: variable que guarda la única instancia, arranca en null
    private static CreateConnection instancia;

    static Properties config = new Properties();

    String hostname = null;
    String port = null;
    String database = null;
    String username = null;
    String password = null;

    // SINGLETON - parte 2: constructor private, nadie fuera puede hacer new CreateConnection()

    private CreateConnection() {
        try (InputStream in = CreateConnection.class.getResourceAsStream("/connection/db_config.properties")) {

            config.load(in);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        loadProperties();
    }

    // SINGLETON - parte 3 = aqui es donde se accede, inicia como null y se devuelve asi mismo, asi siempre tenemos la misma instancia
    public static CreateConnection getInstancia() {
        if (instancia == null) {
            instancia = new CreateConnection();
        }
        return instancia;
    }

    public void loadProperties() {
        this.hostname = config.getProperty("hostname");
        this.port     = config.getProperty("port");
        this.username = config.getProperty("username");
        this.password = config.getProperty("password");
        this.database = config.getProperty("database");
    }
    //esta parte ya la teniamos es el get connection(nos retorna la connection) en este caso solo sera una conexion en todo el codigo
    public Connection getConnection() {
        Connection conn = null;
        try {
            String jdbcUrl = "jdbc:postgresql://" + hostname + ":" + port + "/" + database + "?sslmode=require";
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("Conexion establecida");
        } catch (SQLException ex) {
            Logger.getLogger(CreateConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }
}
