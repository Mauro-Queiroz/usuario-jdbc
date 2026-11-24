package jdbc.usuario.service;

import java.util.List;

import jdbc.usuario.persistence.entity.EmployeeEntity;

/* Interfce define as operações q/Controller poderá solicitar, sem precisar conhecer os detalhes do JDBC ou das consultas SQL.*/
public interface EmployeeService {

    List<EmployeeEntity> findAll();

    EmployeeEntity findById(long id);

    void insert(EmployeeEntity employee);

    int update(EmployeeEntity employee);

    int delete(long id);
	
    // --------------------------------------------------
    // PAGINAÇÃO
    // --------------------------------------------------

    List<EmployeeEntity> findPage(int page, int size);

    long count();  
}