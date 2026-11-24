package jdbc.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jdbc.usuario.persistence.EmployeeParamDAO;
import jdbc.usuario.persistence.entity.EmployeeEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeParamDAO employeeDAO;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeEntity criarFuncionario() {
        EmployeeEntity employee = new EmployeeEntity();

        employee.setId(10016L);
        employee.setName("Benjamin Ferraço");
        employee.setSalary(new BigDecimal("5408.00"));
        employee.setBirthday(
                OffsetDateTime.parse("1987-10-22T03:00:00Z"));

        return employee;
    }

    @Test
    void deveRetornarTodosOsFuncionarios() {
        List<EmployeeEntity> funcionarios =
                List.of(criarFuncionario());

        when(employeeDAO.findAll()).thenReturn(funcionarios);

        List<EmployeeEntity> resultado =
                employeeService.findAll();

        assertEquals(1, resultado.size());
        assertSame(funcionarios, resultado);

        verify(employeeDAO).findAll();
    }

    @Test
    void deveBuscarFuncionarioPorId() {
        EmployeeEntity funcionario = criarFuncionario();

        when(employeeDAO.findById(10016L))
                .thenReturn(funcionario);

        EmployeeEntity resultado =
                employeeService.findById(10016L);

        assertSame(funcionario, resultado);
        assertEquals(10016L, resultado.getId());

        verify(employeeDAO).findById(10016L);
    }

    @Test
    void deveInserirFuncionario() {
        EmployeeEntity funcionario = criarFuncionario();

        employeeService.insert(funcionario);

        verify(employeeDAO).insert(funcionario);
    }

    @Test
    void deveAtualizarFuncionario() {
        EmployeeEntity funcionario = criarFuncionario();

        when(employeeDAO.update(funcionario)).thenReturn(1);

        int resultado = employeeService.update(funcionario);

        assertEquals(1, resultado);

        verify(employeeDAO).update(funcionario);
    }

    @Test
    void deveExcluirFuncionario() {
        when(employeeDAO.delete(10016L)).thenReturn(1);

        int resultado = employeeService.delete(10016L);

        assertEquals(1, resultado);

        verify(employeeDAO).delete(10016L);
    }

    @Test
    void deveBuscarPaginaDeFuncionarios() {
        List<EmployeeEntity> funcionarios =
                List.of(criarFuncionario());

        when(employeeDAO.findPage(0, 10))
                .thenReturn(funcionarios);

        List<EmployeeEntity> resultado =
                employeeService.findPage(0, 10);

        assertEquals(1, resultado.size());
        assertSame(funcionarios, resultado);

        verify(employeeDAO).findPage(0, 10);
    }

    @Test
    void deveContarFuncionarios() {
        when(employeeDAO.count()).thenReturn(10010L);

        long resultado = employeeService.count();

        assertEquals(10010L, resultado);

        verify(employeeDAO).count();
    }
}
