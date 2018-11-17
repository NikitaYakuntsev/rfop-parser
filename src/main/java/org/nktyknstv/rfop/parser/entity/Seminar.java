package org.nktyknstv.rfop.parser.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Seminar extends BaseEntity {
    private String code;
    @Column(columnDefinition = "clob")
    private String description;
    @Column(columnDefinition = "clob")
    private String agenda;
    private String country;
    private String city;
    private String date;
    private String priceIn;
    private String priceOut;

    @ManyToOne
    private Category category;

    @Column(columnDefinition = "clob")
    private String speakers;

    private boolean actual = false;
    private boolean deleted = false;
}
