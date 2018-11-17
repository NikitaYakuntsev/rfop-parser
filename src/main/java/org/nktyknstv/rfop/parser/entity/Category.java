package org.nktyknstv.rfop.parser.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Category extends BaseEntity {

    private String name;

}
