package com.samjay.AI_Powered.Job.Application.Tracker.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Candidates")
public class Candidate extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "firstname")
    private String firstName;

    @Column(value = "lastname")
    private String lastName;

    @Column(value = "email")
    private String email;

    @Column(value = "password")
    private String password;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}