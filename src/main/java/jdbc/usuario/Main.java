package jdbc.usuario;

import java.util.Locale;

import jdbc.usuario.persistence.AccessDAO;  
import jdbc.usuario.persistence.ContactDAO;
import jdbc.usuario.persistence.EmployeeAuditDAO;
import jdbc.usuario.persistence.EmployeeParamDAO;
import jdbc.usuario.persistence.ModuleDAO;
import net.datafaker.Faker;

public class Main {
 
    private final static EmployeeAuditDAO employeeAuditDAO = new EmployeeAuditDAO();
    private final static ContactDAO contactDAO = new ContactDAO();
	private final static AccessDAO accessDAO = new AccessDAO();
	private final static EmployeeParamDAO employeeDAO = 
	                                                 new EmployeeParamDAO(contactDAO, accessDAO);
    private final static ModuleDAO moduleDAO = new ModuleDAO();
	private final static Faker faker = new Faker(new Locale("pt", "BR"));

    public static void main(String[] args) {
    	/*try(var connection = ConnectionUtil.getConnection()) {
			System.out.println("Conectou!");
		}catch(SQLException ex) {
			ex.printStackTrace();
		}*/

        /* var insert = new EmployeeEntity();
        insert.setName("Miguel");
        insert.setSalary(new BigDecimal("2800"));
        insert.setBirthday(OffsetDateTime.now().minusYears(18));
        System.out.println(insert);
        employeeDAO.insert(insert);
		employeeDAO.insertWithProcedure(insert);
        System.out.println(insert);

        employeeDAO.findAll().forEach(System.out::println);

        System.out.println(employeeDAO.findById(1));

        var update = new EmployeeEntity();
        update.setId(insert.getId());
        update.setName("Gabriel");
        update.setSalary(new BigDecimal("5500"));
        update.setBirthday(OffsetDateTime.now().minusYears(36).minusDays(10));
        employeeDAO.update(update);

        employeeDAO.delete(insert.getId());

        employeeAuditDAO.findAll().forEach(System.out::println);

			var entities = Stream.generate(() -> {
				var employee = new EmployeeEntity();
				employee.setName(faker.name().fullName());
				employee.setSalary(new BigDecimal(faker.number().digits(4)));
				employee.setBirthday(OffsetDateTime.of(LocalDate.now().minusYears(faker.number()
				                            .numberBetween(40, 20)), LocalTime.MIN, UTC));
            return employee;
			}).limit(1500).toList();
            //}).limit(10000).toList();
            employeeDAO.insert(entities);

        var employeeUm = new EmployeeEntity();
        employeeUm.setName("Giovani");
        employeeUm.setSalary(new BigDecimal("3200"));
        employeeUm.setBirthday(OffsetDateTime.now().minusYears(25));
        System.out.println(employeeUm);
        employeeDAO.insert(employeeUm);
        System.out.println(employeeUm);

        var contact = new ContactEntity();
        contact.setDescription("miguel@miguel.com");
        contact.setType("e-mail");
        contact.setEmployee(employeeUm);
        contactDAO.insert(contact);

        System.out.println(employeeDAO.findById(1));

		var employeeDois = new EmployeeEntity();
        employeeDois.setName("João");
        employeeDois.setSalary(new BigDecimal("3200"));
        employeeDois.setBirthday(OffsetDateTime.now().minusYears(25));
        System.out.println(employeeDois);
        employeeDAO.insert(employeeDois);
        System.out.println(employeeDois);

        var contact1 = new ContactEntity();
        contact1.setDescription("joao@joao.com");
        contact1.setType("e-mail");
        contact1.setEmployee(employeeDois);
        contactDAO.insert(contact1);

		var employeeTres = new EmployeeEntity();
        employeeTres.setName("Paulo");
        employeeTres.setSalary(new BigDecimal("3200"));
        employeeTres.setBirthday(OffsetDateTime.now().minusYears(25));
        System.out.println(employeeTres);
        employeeDAO.insert(employeeTres);
        System.out.println(employeeTres);

        var contact2 = new ContactEntity();
        contact2.setDescription("33963365002");
        contact2.setType("celular");
        contact2.setEmployee(employeeTres);
        contactDAO.insert(contact2);

        System.out.println(employeeDAO.findById(1));
        employeeDAO.findAll().forEach(System.out::println);

        entities = Stream.generate(() -> {
            var employee = new EmployeeEntity();
            employee.setName(faker.name().fullName());
            employee.setSalary(new BigDecimal(faker.number().digits(4)));
            employee.setBirthday(OffsetDateTime.of(LocalDate.now().minusYears(faker.number().numberBetween(40, 20)), LocalTime.MIN, UTC));
            employee.setModules(new ArrayList<>());

			var moduleAmount = faker.number().numberBetween(1, 4);
            for (int i = 0; i < moduleAmount; i++) {
                var module = new ModuleEntity();
                module.setId(i +1);
                employee.getModules().add(module);
            }
            return employee;
        }).limit(3).toList();
        entities.forEach(employeeDAO::insert);

        moduleDAO.findAll().forEach(System.out::println); */
   }//FIM metodo main()
}//FIM classe MAIN
