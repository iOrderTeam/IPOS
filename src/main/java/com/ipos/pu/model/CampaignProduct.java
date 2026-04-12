package com.ipos.pu.model;

import jakarta.persistence.*;

@Entity
@Table(name = "campaign_products")
public class CampaignProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int hits;
    private int purchased;
    private Double discountOverride;  // null = use campaign's discount

    public Long getID() {
        return id;
    }
    public void setID(Long id) {
        this.id = id;
    }

    public Campaign getCampaign() {
        return campaign;
    }
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }

    public int getHits() {
        return hits;
    }
    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getPurchased() {
        return purchased;
    }
    public void setPurchased(int purchased) {
        this.purchased = purchased;
    }

    public Double getDiscountOverride() {
        return discountOverride;
    }
    public void setDiscountOverride(Double discountOverride) {
        this.discountOverride = discountOverride;
    }

    /** Returns the effective discount for this product (per-product override or campaign default) */
    public double getEffectiveDiscount() {
        return discountOverride != null ? discountOverride : campaign.getDiscountPercentage();
    }
}
