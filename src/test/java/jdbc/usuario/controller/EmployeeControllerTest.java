package jdbc.usuario.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import jdbc.usuario.dto.EmployeePageResponseDTO;
import jdbc.usuario.dto.EmployeeResponseDTO;
import jdbc.usuario.persistence.entity.EmployeeEntity;
import jdbc.usuario.service.EmployeeService;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

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
    void deveListarFuncionariosComPaginacao() {
        List<EmployeeEntity> funcionarios =
                List.of(criarFuncionario());

        when(employeeService.findPage(0, 10))
                .thenReturn(funcionarios);

        when(employeeService.count()).thenReturn(10010L);

        EmployeePageResponseDTO resultado =
                employeeController.findAll(0, 10);

        assertEquals(1, resultado.content().size());
        assertEquals(0, resultado.page());
        assertEquals(10, resultado.size());
        assertEquals(10010L, resultado.totalElements());
        assertEquals(1001, resultado.totalPages());

        verify(employeeService).findPage(0, 10);
        verify(employeeService).count();
    }

    @Test
    void deveRejeitarPaginaNegativa() {
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> employeeController.findAll(-1, 10));

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode());
    }

    @Test
    void deveRejeitarTamanhoDePaginaInvalido() {
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> employeeController.findAll(0, 101));

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode());
    }

    @Test
    void deveBuscarFuncionarioPorId() {
        EmployeeEntity funcionario = criarFuncionario();

        when(employeeService.findById(10016L))
                .thenReturn(funcionario);

        ResponseEntity<EmployeeResponseDTO> resposta =
                employeeController.findById(10016L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(10016L, resposta.getBody().id());
        assertEquals(
                "Benjamin Ferraço",
                resposta.getBody().name());

        verify(employeeService).findById(10016L);
    }

    @Test
    void deveRetornarNotFoundAoBuscarFuncionarioInexistente() {
        when(employeeService.findById(99999L))
                .thenReturn(null);

        ResponseEntity<EmployeeResponseDTO> resposta =
                employeeController.findById(99999L);

        assertEquals(
                HttpStatus.NOT_FOUND,
                resposta.getStatusCode());

        verify(employeeService).findById(99999L);
    }

    @Test
    void deveCadastrarFuncionario() {
        EmployeeEntity funcionario = criarFuncionario();

        EmployeeResponseDTO resposta =
                employeeController.insert(funcionario);

        assertEquals(10016L, resposta.id());
        assertEquals(
                "Benjamin Ferraço",
                resposta.name());

        verify(employeeService).insert(funcionario);
    }

    @Test
    void deveAtualizarFuncionario() {
        EmployeeEntity funcionario = criarFuncionario();

        when(employeeService.update(funcionario)).thenReturn(1);

        ResponseEntity<EmployeeResponseDTO> resposta =
                employeeController.update(10016L, funcionario);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(10016L, resposta.getBody().id());

        verify(employeeService).update(funcionario);
    }

    @Test
    void deveRetornarNotFoundAoAtualizarFuncionarioInexistente() {
        EmployeeEntity funcionario = criarFuncionario();

        when(employeeService.update(funcionario)).thenReturn(0);

        ResponseEntity<EmployeeResponseDTO> resposta =
                employeeController.update(10016L, funcionario);

        assertEquals(
                HttpStatus.NOT_FOUND,
                resposta.getStatusCode());

        verify(employeeService).update(funcionario);
    }

    @Test
    void deveExcluirFuncionario() {
        when(employeeService.delete(10016L)).thenReturn(1);

        ResponseEntity<Void> resposta =
                employeeController.delete(10016L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                resposta.getStatusCode());

        verify(employeeService).delete(10016L);
    }

    @Test
    void deveRetornarNotFoundAoExcluirFuncionarioInexistente() {
        when(employeeService.delete(99999L)).thenReturn(0);

        ResponseEntity<Void> resposta =
                employeeController.delete(99999L);

        assertEquals(
                HttpStatus.NOT_FOUND,
                resposta.getStatusCode());

        verify(employeeService).delete(99999L);
    }
}
