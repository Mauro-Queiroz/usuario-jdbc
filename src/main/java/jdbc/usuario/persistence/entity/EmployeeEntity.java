package jdbc.usuario.persistence.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EmployeeEntity {

    private long id;

    @NotBlank(message = "O nome do funcionário é obrigatório.")
    private String name;

    @NotNull(message = "O salário é obrigatório.")
    @Positive(message = "O salário deve ser maior que zero.")
    private BigDecimal salary;

    @NotNull(message = "A data de nascimento é obrigatória.")
    private OffsetDateTime birthday;

    private List<ContactEntity> contacts;
    private List<ModuleEntity> modules;
}
