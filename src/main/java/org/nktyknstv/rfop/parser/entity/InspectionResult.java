package org.nktyknstv.rfop.parser.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class InspectionResult extends BaseEntity {

    @CreationTimestamp
    private Date inspectionTime;

    @Column(columnDefinition = "clob")
    private String deletedEntities;

    @Column(columnDefinition = "clob")
    private String newEntities;

    @Column(columnDefinition = "clob")
    private String updatedEntities;

    @Column(columnDefinition = "clob")
    private String description;

}
