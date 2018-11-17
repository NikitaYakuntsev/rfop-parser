package org.nktyknstv.rfop.parser.processor;

import org.nktyknstv.rfop.parser.entity.BaseEntity;

import java.util.List;

public abstract class BaseProcessor {
    public abstract List<BaseEntity> process(BaseEntity entity) throws Exception;
}
