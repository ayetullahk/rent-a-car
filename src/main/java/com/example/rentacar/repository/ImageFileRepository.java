package com.example.rentacar.repository;

import com.example.rentacar.domain.ImageFile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageFileRepository extends JpaRepository<ImageFile,String> {

    @EntityGraph(attributePaths = "id")
    List<ImageFile> findAll();

    @EntityGraph(attributePaths = "id")
    Optional<ImageFile> findImageById(String id);
}
