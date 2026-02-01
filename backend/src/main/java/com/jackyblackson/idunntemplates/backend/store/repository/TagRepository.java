package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.backend.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByParentIsNull();
}
