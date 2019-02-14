package com.lucassimao.fluxodecaixa.model;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(
        columnNames = "email",  name="uc_email"
    )
 })
@JsonIgnoreProperties({"role"})
public class User {
    
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String name;
    
    @Email
    @NotEmpty
    @Column(columnDefinition="VARCHAR(200)")
    private String email;

    @JsonProperty("password")
    private String encryptedPassword;
    @NotEmpty
    private String role;

    @CreationTimestamp
    private LocalDateTime signUpDate;



    public User() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEncryptedPassword() {
        return this.encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getSignUpDate() {
        return this.signUpDate;
    }

    public void setSignUpDate(LocalDateTime signUpDate) {
        this.signUpDate = signUpDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof User)) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(encryptedPassword, user.encryptedPassword) && Objects.equals(role, user.role) && Objects.equals(signUpDate, user.signUpDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, encryptedPassword, role, signUpDate);
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", email='" + getEmail() + "'" +
            ", encryptedPassword='" + getEncryptedPassword() + "'" +
            ", role='" + getRole() + "'" +
                ", signUpDate='" + getSignUpDate() + "'" + "}";
    }

    
}