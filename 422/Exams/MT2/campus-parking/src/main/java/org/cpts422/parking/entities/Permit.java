package org.cpts422.parking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

/** A parking permit bought by one user for one zone; records price paid and purchase date. */
@Entity
public class Permit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Zone zone;

    @Column(nullable = false)
    private int pricePaidCents;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    protected Permit() {
    }

    public Permit(User user, Zone zone, int pricePaidCents, LocalDate purchaseDate) {
        this.user = user;
        this.zone = zone;
        this.pricePaidCents = pricePaidCents;
        this.purchaseDate = purchaseDate;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Zone getZone() {
        return zone;
    }

    public int getPricePaidCents() {
        return pricePaidCents;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    @Override
    public String toString() {
        return "Permit{id=" + id + ", userId=" + user.getId() + ", zone=" + zone
                + ", pricePaidCents=" + pricePaidCents + ", purchaseDate=" + purchaseDate + "}";
    }
}
