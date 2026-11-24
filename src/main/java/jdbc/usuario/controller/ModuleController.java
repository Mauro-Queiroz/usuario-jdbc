
package jdbc.usuario.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jdbc.usuario.dto.ModuleEmployeeResponseDTO;
import jdbc.usuario.dto.ModuleResponseDTO;
import jdbc.usuario.persistence.entity.ModuleEntity;
import jdbc.usuario.service.ModuleService;

@RestController
@RequestMapping("/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public List<ModuleResponseDTO> findAll() {

        return moduleService.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private ModuleResponseDTO toResponseDTO(ModuleEntity module) {

        List<ModuleEmployeeResponseDTO> employees =
                module.getEmployees() == null
                        ? List.of()
                        : module.getEmployees()
                                .stream()
                                .map(employee ->
                                        new ModuleEmployeeResponseDTO(
                                                employee.getId(),
                                                employee.getName(),
                                                employee.getSalary(),
                                                employee.getBirthday()))
                                .toList();

        return new ModuleResponseDTO(
                module.getId(),
                module.getName(),
                employees
        );
    }
}
