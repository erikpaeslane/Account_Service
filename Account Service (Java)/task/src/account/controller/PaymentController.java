package account.controller;

import account.model.ChangeUserRoleRequest;
import account.model.PaymentDTO;
import account.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/api/acct/payments")
    public ResponseEntity<?> uploadPayrolls(@RequestBody List<@Valid PaymentDTO> payments) {
        return paymentService.addPayments(payments);
    }

    @PutMapping("/api/acct/payments")
    public ResponseEntity<?> updatePayments(@RequestBody @Valid PaymentDTO payment) {
        return paymentService.updatePayment(payment);
    }

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> getPaymentOfUser(@AuthenticationPrincipal UserDetails user,
                                              @RequestParam(required = false) LocalDate period) {
        System.out.println(user.getUsername());
        return paymentService.getAllPayments(user, period);
    }
}
