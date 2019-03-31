package org.rm3l.datanucleus.gradle.multiproject.sample.jpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.rm3l.datanucleus.gradle.multiproject.sample.jpa.common.MyBaseEntity;

@Entity
public class Person implements MyBaseEntity {
    @Id
    private Long id;

    @Column(
            nullable = false
    )
    private String name;
}
