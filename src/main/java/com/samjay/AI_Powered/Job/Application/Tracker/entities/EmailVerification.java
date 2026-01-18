package com.samjay.AI_Powered.Job.Application.Tracker.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "EmailVerifications")
public class EmailVerification extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "email")
    private String email;

    @Column(value = "verification_code")
    private String verificationCode;

    @Column(value = "is_verified")
    private boolean isVerified;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}