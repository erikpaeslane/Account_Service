package account.service;

import account.entity.Payment;
import account.entity.User;
import account.exception.PaymentAlreadyExistsException;
import account.model.PaymentDTO;
import account.repository.PaymentRepository;
import account.model.PaymentDTOMapper;
import account.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentDTOMapper paymentDTOMapper;
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,UserRepository userRepository,
                          PaymentDTOMapper paymentDTOMapper) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.paymentDTOMapper = paymentDTOMapper;
    }

    @Transactional
    public ResponseEntity<?> addPayments(List<PaymentDTO> paymentsDTO) {
        logger.info("Creating payments...");
        logger.info("Converting payments");
        List<Payment> payments = convertFromDTOToPaymentList(paymentsDTO);
        logger.info("Saving payments...");
        paymentRepository.saveAll(payments);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "Added successfully!"));
    }

    public ResponseEntity<?> getAllPayments(UserDetails userDetails, LocalDate date) {
        logger.info("Getting payments...");
        logger.info("Finding user...");
        User user = userRepository.findUserByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "User not found!"));
        }
        Hibernate.initialize(user.getUserGroups());
        logger.info("Date: {}", date);
        if (date != null)
            return getPaymentOfUserInGivenPeriod(user, date);

        var payments = paymentRepository.findAllByUserOrderByPeriodDesc(user)
                .stream()
                .map(paymentDTOMapper::fromPaymentToPaymentResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(payments);
    }

    public ResponseEntity<?> updatePayment(PaymentDTO paymentDTO) {
        logger.info("Updating user salary...");
        User user = userRepository.findUserByEmail(paymentDTO.employee().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "User not found!"));
        }
        Payment payment = paymentRepository.findByUserAndPeriod(user, paymentDTO.period()).orElseThrow();
        payment.setSalary(paymentDTO.salary());
        paymentRepository.save(payment);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "Updated successfully!"));
    }

    private ResponseEntity<?> getPaymentOfUserInGivenPeriod(User user, LocalDate date) {
        return paymentRepository.findByUserAndPeriod(user, date)
                .map(payment -> ResponseEntity.ok(paymentDTOMapper.fromPaymentToPaymentResponse(payment)))
                .orElseGet(() -> ResponseEntity.ok().body(null));
    }

    public List<Payment> convertFromDTOToPaymentList(List<PaymentDTO> paymentsDTO) {
        List<Payment> payments = new ArrayList<>();

        for (PaymentDTO paymentDTO : paymentsDTO) {
            User user = userRepository.findUserByEmail(paymentDTO.employee().toLowerCase()).orElse(null);
            if (user == null) {
                throw new UsernameNotFoundException("User not found!");
            }
            if (paymentRepository.existsByUserAndPeriod(user, paymentDTO.period())) {
                throw new PaymentAlreadyExistsException("Payment already exists");
            }
            payments.add(paymentDTOMapper.fromPaymentDTOToPayment(paymentDTO, user));
        }
        return payments;
    }

}
