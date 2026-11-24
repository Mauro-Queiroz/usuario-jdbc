package jdbc.usuario.persistence;

import static lombok.AccessLevel.PRIVATE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ConnectionUtil {

    public static Connection getConnection() throws SQLException {

        String url = System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:mysql://localhost:3307/usuario_jdbc?allowPublicKeyRetrieval=true&useSSL=false"
        );

        String username = System.getenv().getOrDefault(
            "DB_USERNAME",
            "springuser"
        );

        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                "A variável de ambiente DB_PASSWORD não foi configurada."
            );
        }

        return DriverManager.getConnection(url, username, password);
    }
}
