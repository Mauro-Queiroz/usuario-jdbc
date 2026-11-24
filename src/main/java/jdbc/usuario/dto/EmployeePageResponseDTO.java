package jdbc.usuario.dto;

import java.util.List;

public record EmployeePageResponseDTO(
        List<EmployeeResponseDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
