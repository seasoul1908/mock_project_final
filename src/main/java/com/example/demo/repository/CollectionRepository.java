package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Repository
public interface CollectionRepository extends JpaRepository<com.example.demo.entity.User, Long> {

    @Query(value = "SELECT collection_id, name, CreatedAt FROM Collections WHERE user_id = :userId ORDER BY CreatedAt DESC", nativeQuery = true)
    List<Map<String, Object>> getAllCollectionsRaw(@Param("userId") long userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO Collections (user_id, Name, CreatedAt) VALUES (:userId, :name, GETDATE())", nativeQuery = true)
    void createCollection(@Param("userId") long userId, @Param("name") String name);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Collections WHERE collection_id = :collectionId AND user_id = :userId", nativeQuery = true)
    void deleteCollection(@Param("collectionId") int collectionId, @Param("userId") long userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Collections SET Name = :name WHERE collection_id = :collectionId AND user_id = :userId", nativeQuery = true)
    void renameCollection(@Param("collectionId") int collectionId, @Param("userId") long userId,
            @Param("name") String name);
}