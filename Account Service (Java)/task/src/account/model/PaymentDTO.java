package account.model;

import account.utils.CustomLocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record PaymentDTO (
        @Email String employee,
        @JsonDeserialize(using = CustomLocalDateDeserializer.class)
        LocalDate period,
        @Min(value = 0, message =  "Salary cannot be negative!") Long salary) {
}
