package edu.wsu.cpts422.reccenter;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final FeeCalculator feeCalculator;
    private final PaymentRepository repository;
    private final PaymentGateway gateway;

    public PaymentService(FeeCalculator feeCalculator, PaymentRepository repository,
                          PaymentGateway gateway) {
        this.feeCalculator = feeCalculator;
        this.repository = repository;
        this.gateway = gateway;
    }

    public Payment pay(PaymentRequest request) {
        validate(request);

        long baseFee = request.getPlan().getBaseFeeCents();
        int percent = feeCalculator.discountPercent(request.getAge(), request.getState());
        long discount = feeCalculator.discountCents(baseFee, percent);
        long amount = baseFee - discount;
        String transactionId = null;

        if (request.getMethod() == PaymentMethod.CARD) {
            amount += feeCalculator.cardSurchargeCents(amount);
            ChargeResult result = gateway.charge(digitsOnly(request.getCardNumber()), amount);
            if (!result.approved()) {
                throw new PaymentDeclinedException(result.message());
            }
            transactionId = result.transactionId();
        }

        Payment payment = new Payment(request.getMemberName(), request.getPlan(), baseFee,
                discount, amount, request.getMethod(), transactionId, LocalDate.now());
        return repository.save(payment);
    }

    public List<Payment> history() {
        return repository.findAllByOrderByReceiptNumberDesc();
    }

    private void validate(PaymentRequest request) {
        Integer age = request.getAge();
        if (age == null || age < 5 || age > 120) {
            throw new InvalidPaymentException("Age must be between 5 and 120");
        }
        if (request.getMethod() == PaymentMethod.CARD
                && !digitsOnly(request.getCardNumber()).matches("\\d{16}")) {
            throw new InvalidPaymentException("Card number must be 16 digits");
        }
    }

    private static String digitsOnly(String cardNumber) {
        return cardNumber == null ? "" : cardNumber.replace(" ", "");
    }
}
