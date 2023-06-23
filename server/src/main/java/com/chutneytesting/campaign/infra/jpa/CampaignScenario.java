package com.chutneytesting.campaign.infra.jpa;

import java.util.List;
import java.util.stream.IntStream;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "CAMPAIGN_SCENARIOS")
public class CampaignScenario {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAMPAIGN_ID")
    private Campaign campaign;

    @Column(name = "SCENARIO_ID")
    private String scenarioId;

    @Column(name = "RANK")
    private Integer rank;

    public CampaignScenario() {
    }

    public CampaignScenario(String scenarioId, Integer rank) {
        this(null, scenarioId, rank);
    }

    public CampaignScenario(Campaign campaign, String scenarioId, Integer rank) {
        this.campaign = campaign;
        this.scenarioId = scenarioId;
        this.rank = rank;
    }

    public String scenarioId() {
        return scenarioId;
    }

    public Campaign campaign() {
        return campaign;
    }

    public Integer rank() {
        return rank;
    }

    public void forCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public static List<CampaignScenario> fromDomain(com.chutneytesting.server.core.domain.scenario.campaign.Campaign campaign) {
        return IntStream.range(0, campaign.scenarioIds.size())
            .mapToObj(idx -> new CampaignScenario(campaign.scenarioIds.get(idx), idx))
            .toList();
    }

    public void rank(Integer rank) {
        this.rank = rank;
    }
}