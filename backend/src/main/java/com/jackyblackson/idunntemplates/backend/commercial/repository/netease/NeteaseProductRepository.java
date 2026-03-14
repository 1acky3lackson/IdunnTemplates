package com.jackyblackson.idunntemplates.backend.commercial.repository.netease;

import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NeteaseProductRepository extends
        JpaRepository<NeteaseProduct, Long>,
        JpaSpecificationExecutor<NeteaseProduct>
{
    Optional<NeteaseProduct> findByItemId(String itemId);

    boolean existsByItemId(String itemId);

    List<NeteaseProduct> findByItemIdIn(Collection<String> itemIds);
}
