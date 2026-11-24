package jdbc.usuario.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record EmployeeResponseDTO(
        long id,
        String name,
        BigDecimal salary,
        OffsetDateTime birthday,
        List<ContactResponseDTO> contacts,
        List<ModuleSummaryDTO> modules
) {
}
