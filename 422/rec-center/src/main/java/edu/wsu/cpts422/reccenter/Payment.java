package edu.wsu.cpts422.reccenter;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

/** One row in the payment history. All money is in cents. */
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long receiptNumber;

    private String memberName;

    @Enumerated(EnumType.STRING)
    private Plan plan;

    private long baseFeeCents;
    private long discountCents;
    private long amountPaidCents;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String transactionId;
    private LocalDate datePaid;

    protected Payment() {
        // required by JPA
    }

    public Payment(String memberName, Plan plan, long baseFeeCents, long discountCents,
                   long amountPaidCents, PaymentMethod method, String transactionId,
                   LocalDate datePaid) {
        this.memberName = memberName;
        this.plan = plan;
        this.baseFeeCents = baseFeeCents;
        this.discountCents = discountCents;
        this.amountPaidCents = amountPaidCents;
        this.method = method;
        this.transactionId = transactionId;
        this.datePaid = datePaid;
    }

    public Long getReceiptNumber() { return receiptNumber; }
    public String getMemberName() { return memberName; }
    public Plan getPlan() { return plan; }
    public long getBaseFeeCents() { return baseFeeCents; }
    public long getDiscountCents() { return discountCents; }
    public long getAmountPaidCents() { return amountPaidCents; }
    public PaymentMethod getMethod() { return method; }
    public String getTransactionId() { return transactionId; }
    public LocalDate getDatePaid() { return datePaid; }
}
