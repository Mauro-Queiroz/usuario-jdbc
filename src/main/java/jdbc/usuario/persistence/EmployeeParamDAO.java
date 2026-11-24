package jdbc.usuario.persistence;

import static java.time.ZoneOffset.UTC;
import java.util.Collections;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement; 
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import jdbc.usuario.persistence.entity.ContactEntity;
import jdbc.usuario.persistence.entity.EmployeeEntity;
import jdbc.usuario.persistence.entity.ModuleEntity;

@Repository
public class EmployeeParamDAO {

    private final ContactDAO contactDAO;
    private final AccessDAO accessDAO;

    public EmployeeParamDAO(ContactDAO contactDAO, AccessDAO accessDAO) {
        this.contactDAO = contactDAO;
        this.accessDAO = accessDAO;
    }

    // --------------------------------------------------
    // INSERT
    // --------------------------------------------------

    public void insert(final EmployeeEntity entity) {

        String sql = """
                INSERT INTO employees (name, salary, birthday)
                VALUES (?, ?, ?)
                """;

        try (var connection = ConnectionUtil.getConnection()) {

            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                try (var statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    statement.setString(1, entity.getName());
                    statement.setBigDecimal(2, entity.getSalary());
                    statement.setTimestamp(3, toTimestamp(entity.getBirthday()));

                    statement.executeUpdate();

                    try (var keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            entity.setId(keys.getLong(1));
                        } else {
                            throw new SQLException(
                                    "O banco não retornou o ID do funcionário.");
                        }
                    }
                }

                // Associa os módulos na mesma transação.
                if (entity.getModules() != null) {
                    for (ModuleEntity module : entity.getModules()) {
                        accessDAO.insert(
                                connection, entity.getId(), module.getId());
                    }
                }
                //---------------------------------
				// Insere os contatos na mesma transação.
				if (entity.getContacts() != null) {
					for (ContactEntity contact : entity.getContacts()) {
						contact.setEmployee(entity);
						contactDAO.insert(connection, contact);
					}
				}
				//---------------------------------
                connection.commit();

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);

                throw new IllegalStateException(
                        "Erro ao inserir funcionário e associar seus módulos.",
                        ex);

            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao acessar o banco para inserir funcionário.", ex);
        }
    }

    // --------------------------------------------------
    // INSERT COM STORED PROCEDURE
    // --------------------------------------------------

    public void insertWithProcedure(final EmployeeEntity entity) {

        try (var connection = ConnectionUtil.getConnection()) {

            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (var statement = connection.prepareCall(
                    "CALL prc_insert_employee(?, ?, ?, ?)")) {

                statement.registerOutParameter(1, Types.BIGINT);
                statement.setString(2, entity.getName());
                statement.setBigDecimal(3, entity.getSalary());
                statement.setTimestamp(4, toTimestamp(entity.getBirthday()));

                statement.execute();

                long generatedId = statement.getLong(1);

                if (statement.wasNull()) {
                    throw new SQLException(
                            "A procedure não retornou o ID do funcionário.");
                }

                entity.setId(generatedId);

                connection.commit();

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);

                throw new IllegalStateException(
                        "Erro ao inserir funcionário pela procedure.", ex);

            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao acessar o banco para executar a procedure.", ex);
        }
    }

    // --------------------------------------------------
    // INSERT EM LOTE
    // --------------------------------------------------

    public void insert(final List<EmployeeEntity> entities) {

        if (entities == null || entities.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO employees (name, salary, birthday)
                VALUES (?, ?, ?)
                """;

        try (var connection = ConnectionUtil.getConnection()) {

            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (var statement = connection.prepareStatement(sql)) {

                for (int i = 0; i < entities.size(); i++) {

                    EmployeeEntity entity = entities.get(i);

                    statement.setString(1, entity.getName());
                    statement.setBigDecimal(2, entity.getSalary());
                    statement.setTimestamp(3, toTimestamp(entity.getBirthday()));

                    statement.addBatch();

                    // Executa lotes de até 1.000 registros.
                    if ((i + 1) % 1000 == 0
                            || i == entities.size() - 1) {

                        statement.executeBatch();
                        statement.clearBatch();
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);

                throw new IllegalStateException(
                        "Erro ao inserir funcionários em lote.", ex);

            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao acessar o banco para inserir funcionários em lote.",
                    ex);
        }
    }

    // --------------------------------------------------
    // UPDATE
    // --------------------------------------------------

    public int update(final EmployeeEntity entity) {

        String sql = """
                UPDATE employees
                   SET name = ?, salary = ?, birthday = ?
                 WHERE id = ?
                """;

        try (var connection = ConnectionUtil.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, entity.getName());
            statement.setBigDecimal(2, entity.getSalary());
            statement.setTimestamp(3, toTimestamp(entity.getBirthday()));
            statement.setLong(4, entity.getId());

            return statement.executeUpdate();

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao atualizar funcionário ID " + entity.getId(), ex);
        }
    }

    // --------------------------------------------------
    // DELETE
    // --------------------------------------------------

    public int delete(final long id) {

        String sqlContacts = """
                DELETE FROM contacts
                WHERE employee_id = ?
                """;

        String sqlAccesses = """
                DELETE FROM accesses
                WHERE employee_id = ?
                """;

        String sqlEmployee = """
                DELETE FROM employees
                WHERE id = ?
                """;

        try (var connection = ConnectionUtil.getConnection()) {

            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                // Primeiro exclui os contatos.
                try (var statement = connection.prepareStatement(sqlContacts)) {
                    statement.setLong(1, id);
                    statement.executeUpdate();
                }

                // Depois exclui as associações com módulos.
                try (var statement = connection.prepareStatement(sqlAccesses)) {
                    statement.setLong(1, id);
                    statement.executeUpdate();
                }

                // Por último, exclui o funcionário.
                int affectedRows;

                try (var statement = connection.prepareStatement(sqlEmployee)) {
                    statement.setLong(1, id);
                    affectedRows = statement.executeUpdate();
                }

                connection.commit();

                return affectedRows;

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);

                throw new IllegalStateException(
                        "Erro ao excluir funcionário ID " + id, ex);

            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao acessar o banco para excluir funcionário ID "
                            + id, ex);
        }
    }
	
	// --------------------------------------------------
    // PAGINAÇÃO - FIND PAGE
    // --------------------------------------------------

    public List<EmployeeEntity> findPage(
            final int page,
            final int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "A página não pode ser negativa.");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "O tamanho da página deve estar entre 1 e 100.");
        }

        long offset = Math.multiplyExact((long) page, size);

        String sql = """
                SELECT id, name, salary, birthday
                FROM employees
                ORDER BY name, id
                LIMIT ? OFFSET ?
                """;

        List<EmployeeEntity> employees = new ArrayList<>();

        try (var connection = ConnectionUtil.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setInt(1, size);
            statement.setLong(2, offset);

            try (var resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    EmployeeEntity employee = new EmployeeEntity();

                    employee.setId(resultSet.getLong("id"));
                    employee.setName(resultSet.getString("name"));
                    employee.setSalary(
                            resultSet.getBigDecimal("salary"));

                    employee.setBirthday(
                            OffsetDateTime.ofInstant(
                                    resultSet.getTimestamp("birthday")
                                            .toInstant(),
                                    UTC));

                    employee.setContacts(new ArrayList<>());
                    employee.setModules(new ArrayList<>());

                    employees.add(employee);
                }
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao consultar a página de funcionários.", ex);
        }

        // Se a página estiver vazia, não precisamos consultar
        // contatos nem módulos.
        if (employees.isEmpty()) {
            return employees;
        }       

        List<Long> employeeIds = employees.stream()
                .map(EmployeeEntity::getId)
                .toList();

        // Busca os contatos somente dos funcionários da página.
        Map<Long, List<ContactEntity>> contactsByEmployee =
                findContactsByEmployeeIds(employeeIds);

        // Busca os módulos somente dos funcionários da página.
        Map<Long, List<ModuleEntity>> modulesByEmployee =
                accessDAO.findModulesByEmployeeIds(employeeIds);

        // Associa contatos e módulos aos respectivos funcionários.
        for (EmployeeEntity employee : employees) {

            employee.setContacts(
                    contactsByEmployee.getOrDefault(
                            employee.getId(),
                            new ArrayList<>()));

            employee.setModules(
                    modulesByEmployee.getOrDefault(
                            employee.getId(),
                            new ArrayList<>()));
        }

        return employees;
    }

    // --------------------------------------------------
    // BUSCAR CONTATOS DOS FUNCIONÁRIOS DA PÁGINA
    // --------------------------------------------------

    private Map<Long, List<ContactEntity>> findContactsByEmployeeIds(
            final List<Long> employeeIds) {

        Map<Long, List<ContactEntity>> contactsByEmployee =
                new LinkedHashMap<>();

        if (employeeIds == null || employeeIds.isEmpty()) {
            return contactsByEmployee;
        }

        String placeholders = String.join(
                ",", Collections.nCopies(employeeIds.size(), "?"));

        String sql = """
                SELECT
                    id,
                    employee_id,
                    description,
                    type
                FROM contacts
                WHERE employee_id IN (
                """ + placeholders + """
                )
                ORDER BY employee_id, id
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

                    ContactEntity contact = new ContactEntity();

                    contact.setId(resultSet.getLong("id"));
                    contact.setDescription(
                            resultSet.getString("description"));
                    contact.setType(resultSet.getString("type"));

                    contactsByEmployee
                            .computeIfAbsent(
                                    employeeId,
                                    key -> new ArrayList<>())
                            .add(contact);
                }
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao consultar contatos da página.", ex);
        }

        return contactsByEmployee;
    }

    // --------------------------------------------------
    // CONTAR FUNCIONÁRIOS
    // --------------------------------------------------

    public long count() {

        String sql = """
                SELECT COUNT(*)
                FROM employees
                """;

        try (var connection = ConnectionUtil.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }

            return 0;

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao contar funcionários.", ex);
        }
    }

    // --------------------------------------------------
    // FIND ALL
    // --------------------------------------------------

    public List<EmployeeEntity> findAll() {

        String sql = """
                SELECT
                    e.id AS employee_id,
                    e.name AS employee_name,
                    e.salary AS employee_salary,
                    e.birthday AS employee_birthday,
                    c.id AS contact_id,
                    c.description AS contact_description,
                    c.type AS contact_type
                FROM employees e
                LEFT JOIN contacts c
                    ON c.employee_id = e.id
                ORDER BY e.name, e.id, c.id
                """;

        Map<Long, EmployeeEntity> employees = new LinkedHashMap<>();

        try (var connection = ConnectionUtil.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                long employeeId = resultSet.getLong("employee_id");

                EmployeeEntity employee = employees.get(employeeId);

                if (employee == null) {
                    employee = new EmployeeEntity();

                    employee.setId(employeeId);
                    employee.setName(resultSet.getString("employee_name"));
                    employee.setSalary(
                            resultSet.getBigDecimal("employee_salary"));

                    employee.setBirthday(
                            OffsetDateTime.ofInstant(
                                    resultSet.getTimestamp("employee_birthday")
                                            .toInstant(),
                                    UTC));

                    employee.setContacts(new ArrayList<>());

                    employees.put(employeeId, employee);
                }

                // O LEFT JOIN pode retornar funcionário sem contato.
                if (resultSet.getObject("contact_id") != null) {

                    ContactEntity contact = new ContactEntity();

                    contact.setId(resultSet.getLong("contact_id"));
                    contact.setDescription(
                            resultSet.getString("contact_description"));
                    contact.setType(resultSet.getString("contact_type"));

                    contact.setEmployee(employee);

                    employee.getContacts().add(contact);
                }
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao listar funcionários e seus contatos.", ex);
        }  
		
		//return new ArrayList<>(employees.values());
		
		List<EmployeeEntity> employeeList =
				new ArrayList<>(employees.values());

		Map<Long, List<ModuleEntity>> modulesByEmployee =
				accessDAO.findModulesByEmployee();

		for (EmployeeEntity employee : employeeList) {

			employee.setModules(
					modulesByEmployee.getOrDefault(
							employee.getId(),
							new ArrayList<>()));
		}

		return employeeList;
    }

    // --------------------------------------------------
    // FIND BY ID
    // --------------------------------------------------

    public EmployeeEntity findById(final long id) {

        String sql = """
                SELECT
                    e.id AS employee_id,
                    e.name AS employee_name,
                    e.salary AS employee_salary,
                    e.birthday AS employee_birthday,
                    c.id AS contact_id,
                    c.description AS contact_description,
                    c.type AS contact_type
                FROM employees e
                LEFT JOIN contacts c
                    ON c.employee_id = e.id
                WHERE e.id = ?
                ORDER BY c.id
                """;

        try (var connection = ConnectionUtil.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (var resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                EmployeeEntity employee = new EmployeeEntity();

                employee.setId(resultSet.getLong("employee_id"));
                employee.setName(resultSet.getString("employee_name"));
                employee.setSalary(resultSet.getBigDecimal("employee_salary"));

                employee.setBirthday(
                        OffsetDateTime.ofInstant(
                                resultSet.getTimestamp("employee_birthday")
                                        .toInstant(),
                                UTC));

                employee.setContacts(new ArrayList<>());

                do {
                    if (resultSet.getObject("contact_id") != null) {

                        ContactEntity contact = new ContactEntity();

                        contact.setId(resultSet.getLong("contact_id"));
                        contact.setDescription(
                                resultSet.getString("contact_description"));
                        contact.setType(resultSet.getString("contact_type"));

                        contact.setEmployee(employee);

                        employee.getContacts().add(contact);
                    }

                } while (resultSet.next());
                
                employee.setModules(
                        accessDAO.findModulesByEmployeeId(id));
				
				return employee;
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Erro ao buscar funcionário ID " + id, ex);
        }
    }

    // --------------------------------------------------
    // MÉTODOS AUXILIARES
    // --------------------------------------------------

    private Timestamp toTimestamp(final OffsetDateTime dateTime) {

        return Timestamp.valueOf(
                dateTime.withOffsetSameInstant(UTC).toLocalDateTime());
    }

    private void rollback(final Connection connection,
                          final Exception originalException) {

        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
