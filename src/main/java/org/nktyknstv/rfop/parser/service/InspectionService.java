package org.nktyknstv.rfop.parser.service;

import org.nktyknstv.rfop.parser.entity.InspectionResult;
import org.nktyknstv.rfop.parser.repository.InspectionResultRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class InspectionService {

    @Resource
    private InspectionResultRepository inspectionResultRepository;

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InspectionResult saveInspectionResult(InspectionResult result) {
        return inspectionResultRepository.save(result);
    }
}
