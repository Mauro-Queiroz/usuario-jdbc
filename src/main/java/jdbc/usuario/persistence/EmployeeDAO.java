package jdbc.usuario.persistence;

import static java.time.ZoneOffset.UTC;

//import com.mysql.cj.jdbc.StatementImpl;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jdbc.usuario.persistence.entity.EmployeeEntity;

public class EmployeeDAO {
	/*public void insert(final EmployeeEntity entity){
        try(
                var connection = ConnectionUtil.getConnection();
                var statement = connection.createStatement()
        ){
            var sql = "INSERT INTO employees (name, salary, birthday) values ('" +
                    entity.getName() + "', " +
                    entity.getSalary().toString() + ", " +
                    "'" + formatOffsetDateTime(entity.getBirthday()) + "' )";
            statement.executeUpdate(sql);
            if (statement instanceof StatementImpl impl)
                entity.setId(impl.getLastInsertID());
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }*/
	public void insert(final EmployeeEntity entity) {
    try (
        var connection = ConnectionUtil.getConnection();
        var statement = connection.prepareStatement(
            "INSERT INTO employees (name, salary, birthday) VALUES (?, ?, ?)",
            java.sql.Statement.RETURN_GENERATED_KEYS
        )
    ) {
        statement.setString(1, entity.getName());
        statement.setBigDecimal(2, entity.getSalary());
        statement.setTimestamp(
            3,
            Timestamp.valueOf(
                entity.getBirthday()
                    .withOffsetSameInstant(UTC)
                    .toLocalDateTime()
            )
        );

        statement.executeUpdate();

        try (var keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                entity.setId(keys.getLong(1));
            }
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

    public void update(final EmployeeEntity entity){
       /* try( var connection = ConnectionUtil.getConnection();
               var statement = connection.createStatement()
        ){  var sql = "UPDATE employees set " +
                    "name     = '" + entity.getName() + "'," +
                    "salary   = " + entity.getSalary().toString() + "," +
                    "birthday = '" + formatOffsetDateTime(entity.getBirthday()) + "'" +
                    "WHERE id = " + entity.getId();
            statement.executeUpdate(sql);
            System.out.printf("Foram afetados %s registros na base de dados", statement.getUpdateCount());
		   if (statement instanceof StatementImpl impl)
                entity.setId(impl.getLastInsertID());
           }catch (SQLException ex){
              ex.printStackTrace();
           }  */
		try (
			var connection = ConnectionUtil.getConnection();
			var statement = connection.prepareStatement(
				"UPDATE employees " +
				"SET name = ?, salary = ?, birthday = ? " +
				"WHERE id = ?"
			)
		){
			statement.setString(1, entity.getName());
			statement.setBigDecimal(2, entity.getSalary());
			statement.setTimestamp(3, Timestamp.valueOf(
					entity.getBirthday()
						.withOffsetSameInstant(UTC)
						.toLocalDateTime()
				)
			);
			statement.setLong(4, entity.getId());
			statement.executeUpdate();
			System.out.printf("Foram afetados %s registros na base de dados%n",
				statement.getUpdateCount() );
			} catch (SQLException ex) {
			ex.printStackTrace();
			}//fim CATCH
    }//FIM update(final EmployeeEntity entity)

	public void delete(final long id) {
    try (
        var connection = ConnectionUtil.getConnection();
        var statement = connection.prepareStatement(
            "DELETE FROM employees WHERE id = ?"
        )
    ) {
        statement.setLong(1, id);
        statement.executeUpdate();

    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}
   /** public void delete(final long id){
        try(
                var connection = ConnectionUtil.getConnection();
                var statement = connection.createStatement()
        ){
            var sql = "DELETE FROM employees WHERE id = " + id;
            statement.executeUpdate(sql);
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }*/
    public List<EmployeeEntity> findAll(){
        List<EmployeeEntity> entities = new ArrayList<>();
        try(
                var connection = ConnectionUtil.getConnection();
                var statement = connection.createStatement()
        ){
            statement.executeQuery("SELECT * FROM employees ORDER BY name");
            var resultSet = statement.getResultSet();
            while (resultSet.next()){
                var entity = new EmployeeEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                entity.setSalary(resultSet.getBigDecimal("salary"));
                var birthdayInstant = resultSet.getTimestamp("birthday").toInstant();
                entity.setBirthday(OffsetDateTime.ofInstant(birthdayInstant, UTC));
                entities.add(entity);
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }
        return entities;
    }
    public EmployeeEntity findById(final long id) {
		var entity = new EmployeeEntity();
		try (
			var connection = ConnectionUtil.getConnection();
			var statement = connection.prepareStatement(
            "SELECT * FROM employees WHERE id = ?" )
		) {
        statement.setLong(1, id);
        try (var resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                entity.setSalary(resultSet.getBigDecimal("salary"));
                var birthdayInstant =
                    resultSet.getTimestamp("birthday").toInstant();
                entity.setBirthday(
                    OffsetDateTime.ofInstant(birthdayInstant, UTC)
                );
            }
        }
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return entity;
	}//findById(final long id) {
	/** public EmployeeEntity findById(final long id){
        var entity = new EmployeeEntity();
        try(
                var connection = ConnectionUtil.getConnection();
                var statement = connection.createStatement()
        ){
            statement.executeQuery("SELECT * FROM employees WHERE id = " + id);
            var resultSet = statement.getResultSet();
            if (resultSet.next()){
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                entity.setSalary(resultSet.getBigDecimal("salary"));
                var birthdayInstant = resultSet.getTimestamp("birthday").toInstant();
                entity.setBirthday(OffsetDateTime.ofInstant(birthdayInstant, UTC));
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }
        return entity;
    }*/
    private String formatOffsetDateTime(final OffsetDateTime dateTime){
        var utcDatetime = dateTime.withOffsetSameInstant(UTC);
        return utcDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
