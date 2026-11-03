package org.cpts422.parking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A registered student or employee with one vehicle license plate. */
@Entity
@Table(name = "users") // USER is a reserved word in H2
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    protected User() {
    }

    public User(String name, UserType type, String licensePlate) {
        this.name = name;
        this.type = type;
        this.licensePlate = licensePlate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserType getType() {
        return type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', type=" + type
                + ", licensePlate='" + licensePlate + "'}";
    }
}
