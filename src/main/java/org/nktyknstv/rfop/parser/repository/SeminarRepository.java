package org.nktyknstv.rfop.parser.repository;

import org.nktyknstv.rfop.parser.entity.Seminar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SeminarRepository extends JpaRepository<Seminar, String> {

    @Modifying
    @Query("update Seminar s set s.actual = false where s.deleted = false")
    void setActualFalse();

    List<Seminar> findAllByActualFalseAndDeletedFalse();

    Optional<Seminar> findByCode(String code);
}
