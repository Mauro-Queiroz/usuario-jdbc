package jdbc.usuario.persistence;

import static java.time.ZoneOffset.UTC;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import jdbc.usuario.persistence.entity.EmployeeEntity;
import jdbc.usuario.persistence.entity.ModuleEntity;

@Repository
public class ModuleDAO {

    public List<ModuleEntity> findAll() {

        String sql = """
                SELECT
                    m.id AS module_id,
                    m.name AS module_name,
                    e.id AS employee_id,
                    e.name AS employee_name,
                    e.salary AS employee_salary,
                    e.birthday AS employee_birthday
                FROM modules m
                LEFT JOIN accesses a
                    ON a.module_id = m.id
                LEFT JOIN employees e
                    ON e.id = a.employee_id
                ORDER BY m.id, e.id
                """;

        Map<Long, ModuleEntity> modules = new LinkedHashMap<>();

        try (var connection = ConnectionUtil.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                long moduleId = resultSet.getLong("module_id");

                ModuleEntity module = modules.get(moduleId);

                if (module == null) {
                    module = new ModuleEntity();
                    module.setId(moduleId);
                    module.setName(resultSet.getString("module_name"));
                    module.setEmployees(new ArrayList<>());

                    modules.put(moduleId, module);
                }

                // Um módulo pode não possuir funcionários.
                if (resultSet.getObject("employee_id") != null) {

                    EmployeeEntity employee = new EmployeeEntity();

                    employee.setId(resultSet.getLong("employee_id"));
                    employee.setName(resultSet.getString("employee_name"));
                    employee.setSalary(
                            resultSet.getBigDecimal("employee_salary"));

                    var birthday = resultSet.getTimestamp("employee_birthday");

                    employee.setBirthday(
                            OffsetDateTime.ofInstant(
                                    birthday.toInstant(), UTC));

                    module.getEmployees().add(employee);
                }
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao consultar os módulos e seus funcionários.", ex);
        }

        return new ArrayList<>(modules.values());
    }
}