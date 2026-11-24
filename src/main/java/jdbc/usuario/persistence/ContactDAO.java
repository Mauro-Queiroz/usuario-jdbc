package jdbc.usuario.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import jdbc.usuario.persistence.entity.ContactEntity;
import jdbc.usuario.persistence.entity.EmployeeEntity;

@Repository
public class ContactDAO {

    public void insert(final ContactEntity entity) {
        try (var connection = ConnectionUtil.getConnection()) {
            insert(connection, entity);
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inserir contato.", ex);
        }
    }

    // Utiliza a conexão da transação iniciada por outro DAO.
    void insert(final Connection connection, final ContactEntity entity)
            throws SQLException {

        String sql = """
                INSERT INTO contacts (description, type, employee_id)
                VALUES (?, ?, ?)
                """;

        try (var statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, entity.getDescription());
            statement.setString(2, entity.getType());
            statement.setLong(3, entity.getEmployee().getId());

            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<ContactEntity> findByEmployeeId(final long employeeId) {
        String sql = """
                SELECT id, description, type, employee_id
                FROM contacts
                WHERE employee_id = ?
                ORDER BY id
                """;

        List<ContactEntity> entities = new ArrayList<>();

        try (var connection = ConnectionUtil.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setLong(1, employeeId);

            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ContactEntity entity = new ContactEntity();

                    entity.setId(resultSet.getLong("id"));
                    entity.setDescription(resultSet.getString("description"));
                    entity.setType(resultSet.getString("type"));

                    EmployeeEntity employee = new EmployeeEntity();
                    employee.setId(resultSet.getLong("employee_id"));

                    entity.setEmployee(employee);
                    entities.add(entity);
                }
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao consultar contatos do funcionário ID "
                            + employeeId, ex);
        }

        return entities;
    }
}
