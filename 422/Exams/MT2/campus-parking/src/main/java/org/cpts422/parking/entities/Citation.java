package org.cpts422.parking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

/** A parking citation issued to one user; starts unpaid (R8) and is marked paid by R9. */
@Entity
public class Citation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private LocalDate issuedDate;

    @Column(nullable = false)
    private int fineCents;

    @Column(nullable = false)
    private boolean paid;

    protected Citation() {
    }

    public Citation(User user, LocalDate issuedDate, int fineCents) {
        this.user = user;
        this.issuedDate = issuedDate;
        this.fineCents = fineCents;
        this.paid = false;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public int getFineCents() {
        return fineCents;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @Override
    public String toString() {
        return "Citation{id=" + id + ", userId=" + user.getId() + ", issuedDate=" + issuedDate
                + ", fineCents=" + fineCents + ", paid=" + paid + "}";
    }
}
