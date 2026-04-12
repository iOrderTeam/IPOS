package com.ipos.pu.repository;

import com.ipos.pu.model.Campaign;
import com.ipos.pu.model.CampaignProduct;
import com.ipos.pu.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignProductRepository extends JpaRepository<CampaignProduct,Long> {
    List<CampaignProduct> findByCampaign(Campaign campaign);
    List<CampaignProduct> findByProduct(Product product);
    Optional<CampaignProduct> findByCampaignAndProduct(Campaign campaign, Product product);
    void deleteByCampaign(Campaign campaign);
}
