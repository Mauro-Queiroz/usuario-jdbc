package jdbc.usuario.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ModuleEmployeeResponseDTO(
        long id,
        String name,
        BigDecimal salary,
        OffsetDateTime birthday
) {
}
