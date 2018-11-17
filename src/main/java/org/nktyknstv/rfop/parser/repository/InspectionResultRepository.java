package org.nktyknstv.rfop.parser.repository;

import org.nktyknstv.rfop.parser.entity.InspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionResultRepository extends JpaRepository<InspectionResult, String> {
}
