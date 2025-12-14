package com.mmb.repository;

import com.mmb.entity.DailyWordItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyWordItemRepository extends JpaRepository<DailyWordItem, Integer> {
    List<DailyWordItem> findBySetIdOrderBySortOrderAsc(Integer setId);
}
