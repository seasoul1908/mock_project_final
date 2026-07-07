package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Report;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatusOrderByCreatedAtDesc(String status);

    List<Report> findAllByOrderByCreatedAtDesc();

    // Deletion suggestions are stored as Reports with note = 'DELETION_SUGGESTION'
    List<Report> findByNoteOrderByCreatedAtDesc(String note);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    int countByStatus(@Param("status") String status);
}