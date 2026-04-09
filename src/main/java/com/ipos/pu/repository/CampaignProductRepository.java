package com.ipos.pu.repository;

import com.ipos.pu.model.Campaign;
import com.ipos.pu.model.CampaignProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampaignProductRepository extends JpaRepository<CampaignProduct,Long> {
    List<CampaignProduct> findByCampaign(Campaign campaign);
}
