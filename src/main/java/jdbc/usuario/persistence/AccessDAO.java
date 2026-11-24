package jdbc.usuario.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import org.springframework.stereotype.Repository;

import jdbc.usuario.persistence.entity.ModuleEntity;

@Repository
public class AccessDAO {

    public void insert(final long employeeId, final long moduleId) {
        try (var connection = ConnectionUtil.getConnection()) {
            insert(connection, employeeId, moduleId);
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao associar funcionário ao módulo.", ex);
        }
    }

    // Participa da transação iniciada pelo EmployeeParamDAO.
    void insert(final Connection connection,
                final long employeeId,
                final long moduleId) throws SQLException {

        String sql = """
                INSERT INTO accesses (employee_id, module_id)
                VALUES (?, ?)
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            statement.setLong(2, moduleId);
            statement.executeUpdate();
        }
    }

    // Busca os módulos de todos os funcionários em uma única consulta.
    public Map<Long, List<ModuleEntity>> findModulesByEmployee() {
        return findModules(null);
    }

    // Busca os módulos de um funcionário específico.
    public List<ModuleEntity> findModulesByEmployeeId(final long employeeId) {
        return findModules(employeeId)
                .getOrDefault(employeeId, List.of());
    }

    private Map<Long, List<ModuleEntity>> findModules(
            final Long employeeId) {

        String sql = """
                SELECT
                    a.employee_id,
                    m.id AS module_id,
                    m.name AS module_name
                FROM accesses a
                INNER JOIN modules m
                    ON m.id = a.module_id
                """
                + (employeeId == null ? "" : " WHERE a.employee_id = ? ")
                + """
                ORDER BY a.employee_id, m.id
                """;

        Map<Long, List<ModuleEntity>> modulesByEmployee =
                new LinkedHashMap<>();

        try (var connection = ConnectionUtil.getConnection();
             var statement = connection.prepareStatement(sql)) {

            if (employeeId != null) {
                statement.setLong(1, employeeId);
            }

            try (var resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    long id = resultSet.getLong("employee_id");

                    ModuleEntity module = new ModuleEntity();
                    module.setId(resultSet.getLong("module_id"));
                    module.setName(resultSet.getString("module_name"));

                    modulesByEmployee
                            .computeIfAbsent(id, key -> new ArrayList<>())
                            .add(module);
                }
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao consultar os módulos dos funcionários.", ex);
        }

        return modulesByEmployee;
    }
	// Busca os módulos somente dos funcionários da página.
    public Map<Long, List<ModuleEntity>> findModulesByEmployeeIds(
            final Collection<Long> employeeIds) {

        Map<Long, List<ModuleEntity>> modulesByEmployee =
                new LinkedHashMap<>();

        if (employeeIds == null || employeeIds.isEmpty()) {
            return modulesByEmployee;
        }
  
        String placeholders = String.join(
                ",", java.util.Collections.nCopies(
                        employeeIds.size(), "?"));

        String sql = """
                SELECT
                    a.employee_id,
                    m.id AS module_id,
                    m.name AS module_name
                FROM accesses a
                INNER JOIN modules m
                    ON m.id = a.module_id
                WHERE a.employee_id IN (
                """ + placeholders + """
                )
                ORDER BY a.employee_id, m.id
                """;

        try (var connection = ConnectionUtil.getConnection();
             var statement = connection.prepareStatement(sql)) {

            int index = 1;

            for (Long employeeId : employeeIds) {
                statement.setLong(index++, employeeId);
            }

            try (var resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    long employeeId =
                            resultSet.getLong("employee_id");

                    ModuleEntity module = new ModuleEntity();

                    module.setId(resultSet.getLong("module_id"));
                    module.setName(resultSet.getString("module_name"));

                    modulesByEmployee
                            .computeIfAbsent(
                                    employeeId,
                                    key -> new ArrayList<>())
                            .add(module);
                }
            }

        } catch (SQLException ex) {   
            throw new IllegalStateException(
                    "Erro ao consultar módulos dos funcionários da página.",
                    ex);
        }

        return modulesByEmployee;
    }//FIM metodo findModulesByEmployeeIds(
}
