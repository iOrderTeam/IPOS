package com.ipos.pu.service;

import com.ipos.pu.model.*;
import com.ipos.pu.repository.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final CampaignRepository campaignRepository;
    private final CampaignProductRepository campaignProductRepository;
    private final ProductRepository productRepository;

    public AdminService(CampaignRepository campaignRepository,
                        CampaignProductRepository campaignProductRepository,
                        ProductRepository productRepository) {
        this.campaignRepository = campaignRepository;
        this.campaignProductRepository = campaignProductRepository;
        this.productRepository = productRepository;
    }

    // UC18 - Create a campaign
    public Campaign createCampaign(String name, String description,
                                   double discountPercentage,
                                   LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        Campaign campaign = new Campaign();
        campaign.setName(name);
        campaign.setDescription(description);
        campaign.setDiscountPercentage(discountPercentage);
        campaign.setStartDate(startDate);
        campaign.setEndDate(endDate);
        campaign.setHits(0);
        campaign.setActive(true);
        return campaignRepository.save(campaign);
    }

    // UC18 - Modify a campaign (change discounts and dates)
    public Campaign modifyCampaign(Long campaignId, double discountPercentage,
                                   LocalDate startDate, LocalDate endDate) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        campaign.setDiscountPercentage(discountPercentage);
        campaign.setStartDate(startDate);
        campaign.setEndDate(endDate);
        return campaignRepository.save(campaign);
    }

    // UC18 - Delete a campaign
    @Transactional
    public void deleteCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        campaignProductRepository.deleteByCampaign(campaign);
        campaignRepository.deleteById(campaignId);
    }

    // UC18 - Terminate a campaign early
    public Campaign terminateCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        campaign.setActive(false);
        campaign.setEndDate(LocalDate.now());
        return campaignRepository.save(campaign);
    }

    // UC10 - Get currently active campaigns (active flag AND within date range)
    public List<Campaign> getActiveCampaigns() {
        LocalDate today = LocalDate.now();
        return campaignRepository
                .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
    }

    // Get all campaigns (for admin view)
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    // UC12 - Increment campaign hits counter
    public void incrementCampaignHits(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        campaign.setHits(campaign.getHits() + 1);
        campaignRepository.save(campaign);
    }

    // Increment per-product hit counter within a campaign
    public void incrementProductHits(Long campaignId, Long productId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        CampaignProduct cp = campaignProductRepository.findByCampaignAndProduct(campaign, product)
                .orElseThrow(() -> new IllegalArgumentException("Product not in campaign."));
        cp.setHits(cp.getHits() + 1);
        campaignProductRepository.save(cp);
    }

    // Add a product to a campaign with an optional per-product discount override
    public CampaignProduct addProductToCampaign(Long campaignId, Long productId, Double discountOverride) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        if (campaignProductRepository.findByCampaignAndProduct(campaign, product).isPresent()) {
            throw new IllegalArgumentException("Product already in this campaign.");
        }

        CampaignProduct cp = new CampaignProduct();
        cp.setCampaign(campaign);
        cp.setProduct(product);
        cp.setHits(0);
        cp.setDiscountOverride(discountOverride);
        return campaignProductRepository.save(cp);
    }

    // Remove a product from a campaign
    public void removeProductFromCampaign(Long campaignProductId) {
        campaignProductRepository.deleteById(campaignProductId);
    }

    // Get products in a campaign
    public List<CampaignProduct> getCampaignProducts(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        return campaignProductRepository.findByCampaign(campaign);
    }

    // Check for overlapping campaigns on a product (different discounts for same product)
    public List<String> checkOverlappingCampaigns(Long campaignId, Long productId) {
        Campaign thisCampaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        List<CampaignProduct> allCps = campaignProductRepository.findByProduct(product);
        List<String> conflicts = new ArrayList<>();

        for (CampaignProduct cp : allCps) {
            Campaign other = cp.getCampaign();
            if (other.getId() == thisCampaign.getId()) continue;
            if (!other.isActive()) continue;

            boolean overlaps = !thisCampaign.getStartDate().isAfter(other.getEndDate())
                    && !thisCampaign.getEndDate().isBefore(other.getStartDate());

            if (overlaps && other.getDiscountPercentage() != thisCampaign.getDiscountPercentage()) {
                conflicts.add("\"" + other.getName() + "\" offers "
                        + other.getDiscountPercentage() + "% discount on "
                        + product.getName() + " (vs " + thisCampaign.getDiscountPercentage()
                        + "% in this campaign). The higher discount ("
                        + Math.max(other.getDiscountPercentage(), thisCampaign.getDiscountPercentage())
                        + "%) will be applied to the customer.");
            }
        }
        return conflicts;
    }

    // Get the best (highest) active discount for a product right now
    public double getBestDiscount(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        List<CampaignProduct> cps = campaignProductRepository.findByProduct(product);
        LocalDate today = LocalDate.now();
        double best = 0;
        for (CampaignProduct cp : cps) {
            Campaign c = cp.getCampaign();
            if (c.isActive()
                    && !today.isBefore(c.getStartDate())
                    && !today.isAfter(c.getEndDate())) {
                best = Math.max(best, cp.getEffectiveDiscount());
            }
        }
        return best;
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
