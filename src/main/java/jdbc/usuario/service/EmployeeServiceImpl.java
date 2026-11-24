package jdbc.usuario.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jdbc.usuario.persistence.EmployeeParamDAO;
import jdbc.usuario.persistence.entity.EmployeeEntity;

/* Observe anotação @Service: ela informa ao Spring q/essa classe é um componente da camada de serviço. O construtor recebe o DAO por injeção de dependência, em vez de criar uma instância manualmente.*/
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeParamDAO employeeDAO;

    public EmployeeServiceImpl(EmployeeParamDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    @Override
    public List<EmployeeEntity> findAll() {
        return employeeDAO.findAll();
    }

    @Override
    public EmployeeEntity findById(long id) {
        return employeeDAO.findById(id);
    }

    @Override
    public void insert(EmployeeEntity employee) {
        employeeDAO.insert(employee);
    }

    @Override
    public int update(EmployeeEntity employee) {
        return employeeDAO.update(employee);
    }

    @Override
    public int delete(long id) {
        return employeeDAO.delete(id);
    }
	// --------------------------------------------------
    // PAGINAÇÃO
    // --------------------------------------------------

    @Override
    public List<EmployeeEntity> findPage(int page, int size) {
        return employeeDAO.findPage(page, size);
    }

    @Override
    public long count() {
        return employeeDAO.count();
    }

	
}
