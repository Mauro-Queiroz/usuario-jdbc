package jdbc.usuario.dto;

import java.util.List;

public record ModuleResponseDTO(
        long id,
        String name,
        List<ModuleEmployeeResponseDTO> employees
) {
}
