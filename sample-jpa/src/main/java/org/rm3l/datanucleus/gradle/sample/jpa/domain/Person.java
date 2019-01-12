package org.rm3l.datanucleus.gradle.sample.jpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Person {
    @Id
    private Long id;

    @Column(
            nullable = false
    )
    private String name;
}
