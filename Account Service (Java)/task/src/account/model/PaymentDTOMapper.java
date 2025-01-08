package account.model;

import account.entity.Payment;
import account.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class PaymentDTOMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM-yyyy");

    public PaymentDTO fromPaymentToPaymentDTO(Payment payment) {
        return new PaymentDTO(
                payment.getUser().getEmail(),
                payment.getPeriod(),
                payment.getSalary()
        );
    }

    public Payment fromPaymentDTOToPayment(PaymentDTO paymentDTO, User user) {
        return Payment.builder()
                .user(user)
                .period(paymentDTO.period())
                .salary(paymentDTO.salary())
                .build();
    }

    public PaymentResponse fromPaymentToPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getUser().getName(),
                payment.getUser().getLastname(),
                convertDateToString(payment.getPeriod()),
                convertSalaryToString(payment.getSalary())
        );
    }

    private String convertSalaryToString(long salary) {
        long dollars = salary / 100;
        long cents = salary % 100;

        return dollars + " dollar(s) " + cents + " cent(s)";
    }

    private String convertDateToString(LocalDate date) {
        return formatter.format(date);
    }

}
