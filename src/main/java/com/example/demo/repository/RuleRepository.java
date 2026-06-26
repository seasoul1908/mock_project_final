package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Rule;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
     List<Rule> findAllByOrderByCreatedAtDesc();
}
