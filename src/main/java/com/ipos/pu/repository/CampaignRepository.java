package com.ipos.pu.repository;

import com.ipos.pu.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign,Long> {
    List<Campaign> findByStartDateBeforeAndEndDateAfter(LocalDate now, LocalDate now2);
}
