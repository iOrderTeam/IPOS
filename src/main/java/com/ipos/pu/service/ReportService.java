package com.ipos.pu.service;

import com.ipos.pu.model.*;
import com.ipos.pu.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignProductRepository campaignProductRepository;

    public ReportService(OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         CampaignRepository campaignRepository,
                         CampaignProductRepository campaignProductRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.campaignRepository = campaignRepository;
        this.campaignProductRepository = campaignProductRepository;
    }


    public Map<String, Object> generateSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByPlacedAtBetween(from, to);

        Map<Long, Map<String, Object>> productSales = new LinkedHashMap<>();
        int totalQuantity = 0;
        double grandTotal = 0.0;

        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            for (OrderItem item : items) {
                Long productId = item.getProduct().getId();
                Map<String, Object> entry = productSales.computeIfAbsent(productId, k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("productId", productId);
                    m.put("productName", item.getProduct().getName());
                    m.put("unitPrice", item.getPriceAtTimeOfOrder());
                    m.put("quantitySold", 0);
                    m.put("totalSales", 0.0);
                    return m;
                });
                int qty = (int) entry.get("quantitySold") + item.getQuantity();
                double total = (double) entry.get("totalSales") + (item.getQuantity() * item.getPriceAtTimeOfOrder());
                entry.put("quantitySold", qty);
                entry.put("totalSales", total);
                totalQuantity += item.getQuantity();
                grandTotal += item.getQuantity() * item.getPriceAtTimeOfOrder();
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        result.put("items", new ArrayList<>(productSales.values()));
        result.put("totalQuantity", totalQuantity);
        result.put("grandTotal", grandTotal);
        return result;
    }


    public Map<String, Object> generateCampaignReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());

        List<Campaign> allCampaigns = campaignRepository.findAll();

        List<Campaign> campaigns = new ArrayList<>();
        for (Campaign c : allCampaigns) {
            if (c.getStartDate() != null && c.getEndDate() != null
                    && !c.getEndDate().isBefore(startDate)
                    && !c.getStartDate().isAfter(endDate)) {
                campaigns.add(c);
            }
        }

        List<Map<String, Object>> campaignList = new ArrayList<>();
        double allCampaignTotal = 0.0;

        for (Campaign campaign : campaigns) {
            Map<String, Object> campaignData = new LinkedHashMap<>();
            campaignData.put("campaignId", campaign.getName());
            campaignData.put("startDate", campaign.getStartDate().toString());
            campaignData.put("endDate", campaign.getEndDate().toString());
            campaignData.put("discountPercentage", campaign.getDiscountPercentage());

            List<CampaignProduct> campaignProducts = campaignProductRepository.findByCampaign(campaign);
            campaignData.put("itemsIncluded", campaignProducts.size());

            LocalDateTime campStart = campaign.getStartDate().atStartOfDay();
            LocalDateTime campEnd = campaign.getEndDate().atTime(LocalTime.MAX);
            List<Order> campaignOrders = orderRepository.findByPlacedAtBetween(campStart, campEnd);

            List<Map<String, Object>> soldItems = new ArrayList<>();
            double campaignTotal = 0.0;

            for (CampaignProduct cp : campaignProducts) {
                int itemsSold = 0;
                double itemTotal = 0.0;

                for (Order order : campaignOrders) {
                    List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                    for (OrderItem oi : orderItems) {
                        if (oi.getProduct().getId().equals(cp.getProduct().getId())) {
                            itemsSold += oi.getQuantity();
                            double discountedPrice = oi.getPriceAtTimeOfOrder() * (1 - cp.getEffectiveDiscount() / 100.0);
                            itemTotal += oi.getQuantity() * discountedPrice;
                        }
                    }
                }

                Map<String, Object> itemData = new LinkedHashMap<>();
                itemData.put("productId", cp.getProduct().getId());
                itemData.put("productName", cp.getProduct().getName());
                itemData.put("discount", cp.getEffectiveDiscount());
                itemData.put("itemsSold", itemsSold);
                itemData.put("totalSales", itemTotal);
                soldItems.add(itemData);
                campaignTotal += itemTotal;
            }

            campaignData.put("soldItems", soldItems);
            campaignData.put("totalSalesInCampaign", campaignTotal);
            campaignList.add(campaignData);
            allCampaignTotal += campaignTotal;
        }

        result.put("activeCampaigns", campaigns.size());
        result.put("campaigns", campaignList);
        result.put("grandTotal", allCampaignTotal);
        return result;
    }


    public Map<String, Object> generateCampaignEngagementReport(Long campaignDbId) {
        Campaign campaign = campaignRepository.findById(campaignDbId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("campaignId", campaign.getName());
        result.put("description", campaign.getDescription());
        result.put("startDate", campaign.getStartDate().toString());
        result.put("endDate", campaign.getEndDate().toString());

        List<Map<String, Object>> counters = new ArrayList<>();

        List<CampaignProduct> campaignProducts = campaignProductRepository.findByCampaign(campaign);

        int campaignTotalPurchases = 0;
        for (CampaignProduct cp : campaignProducts) {
            campaignTotalPurchases += cp.getPurchased();
        }

        Map<String, Object> campaignCounter = new LinkedHashMap<>();
        campaignCounter.put("counterId", campaign.getName());
        campaignCounter.put("counterDescription", "Campaign hits");
        int campaignHits = campaign.getHits();
        campaignCounter.put("hitsCount", campaignHits);
        campaignCounter.put("purchases", campaignTotalPurchases);
        if (campaignHits > 0) {
            double ratio = (double) campaignTotalPurchases / campaignHits;
            campaignCounter.put("conversionRate",
                    campaignTotalPurchases + "/" + campaignHits + " = " + String.format("%.2f", ratio)
                            + " (" + String.format("%.1f", ratio * 100) + "%)");
        } else {
            campaignCounter.put("conversionRate", "0/0 = 0.00 (0.0%)");
        }
        counters.add(campaignCounter);

        LocalDateTime campStart = campaign.getStartDate().atStartOfDay();
        LocalDateTime campEnd = campaign.getEndDate().atTime(LocalTime.MAX);
        List<Order> campaignOrders = orderRepository.findByPlacedAtBetween(campStart, campEnd);

        int itemIndex = 0;
        for (CampaignProduct cp : campaignProducts) {
            itemIndex++;
            int purchased = cp.getPurchased();

            Map<String, Object> itemCounter = new LinkedHashMap<>();
            itemCounter.put("counterId", "Item(" + itemIndex + ") hits");
            itemCounter.put("counterDescription", "\"" + cp.getProduct().getName() + "\" hits");
            int itemHits = cp.getHits();
            itemCounter.put("hitsCount", itemHits);
            itemCounter.put("purchases", purchased);
            if (itemHits > 0) {
                double ratio = (double) purchased / itemHits;
                itemCounter.put("conversionRate",
                        purchased + "/" + itemHits + " = " + String.format("%.2f", ratio)
                                + " (" + String.format("%.1f", ratio * 100) + "%)");
            } else {
                itemCounter.put("conversionRate", "0/0 = 0.00 (0.0%)");
            }
            counters.add(itemCounter);
        }

        result.put("counters", counters);
        return result;
    }
}
