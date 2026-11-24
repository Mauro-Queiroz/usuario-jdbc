package jdbc.usuario.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import jdbc.usuario.dto.EmployeePageResponseDTO;
import jakarta.validation.Valid;
import jdbc.usuario.dto.ContactResponseDTO;
import jdbc.usuario.dto.EmployeeResponseDTO;
import jdbc.usuario.dto.ModuleSummaryDTO;
import jdbc.usuario.persistence.entity.EmployeeEntity;
import jdbc.usuario.service.EmployeeService;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // --------------------------------------------------
    // FIND ALL - PAGINAÇÃO
    // --------------------------------------------------

    @GetMapping
    public EmployeePageResponseDTO findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A página não pode ser negativa.");
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O tamanho da página deve estar entre 1 e 100.");
        }

        List<EmployeeResponseDTO> employees =
                employeeService.findPage(page, size)
                        .stream()
                        .map(this::toResponseDTO)
                        .toList();

        long totalElements = employeeService.count();

        int totalPages = totalElements == 0
                ? 0
                : (int) (totalElements / size
                        + (totalElements % size == 0 ? 0 : 1));

        return new EmployeePageResponseDTO(
                employees,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    // --------------------------------------------------
    // FIND BY ID
    // --------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> findById(
            @PathVariable long id) {

        EmployeeEntity employee = employeeService.findById(id);

        if (employee == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponseDTO(employee));
    }

    // --------------------------------------------------
    // INSERT
    // --------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponseDTO insert(
            @Valid @RequestBody EmployeeEntity employee) {

        employeeService.insert(employee);

        return toResponseDTO(employee);
    }

    // --------------------------------------------------
    // UPDATE
    // --------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(
            @PathVariable long id,
            @Valid @RequestBody EmployeeEntity employee) {

        employee.setId(id);

        int rowsAffected = employeeService.update(employee);

        if (rowsAffected == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponseDTO(employee));
    }

    // --------------------------------------------------
    // DELETE
    // --------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {

        int rowsAffected = employeeService.delete(id);

        if (rowsAffected == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------
    // CONVERSÃO ENTITY PARA DTO
    // --------------------------------------------------

    private EmployeeResponseDTO toResponseDTO(EmployeeEntity employee) {

        List<ContactResponseDTO> contacts =
                employee.getContacts() == null
                        ? List.of()
                        : employee.getContacts()
                                .stream()
                                .map(contact -> new ContactResponseDTO(
                                        contact.getId(),
                                        contact.getDescription(),
                                        contact.getType()))
                                .toList();

        List<ModuleSummaryDTO> modules =   
                employee.getModules() == null   
                        ? List.of()
                        : employee.getModules()
                                .stream()
                                .map(module -> new ModuleSummaryDTO(
                                        module.getId(),
                                        module.getName()))
                                .toList();

        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getName(),
                employee.getSalary(),
                employee.getBirthday(),   
                contacts,
                modules
        );
    }
}
