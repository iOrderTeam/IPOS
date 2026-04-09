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
}
