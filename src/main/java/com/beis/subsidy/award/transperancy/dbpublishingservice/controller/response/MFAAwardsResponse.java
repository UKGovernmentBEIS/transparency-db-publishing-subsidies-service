package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAGrouping;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MFAAwardsResponse {
    private long activeMfaAwards;
    private long deletedMfaAwards;
    private long allMfaAwards;
    public long totalSearchResults;
    public int currentPage;
    public int totalPages;
    @JsonProperty
    public List<MFAAwardResponse> mfaAwards;

    public MFAAwardsResponse(List<MFAAward> mfaAwards, long totalSearchResults,
                             int currentPage, int totalPages, Map<String, Long> mfaGroupingsCounts) {
        this.mfaAwards = mfaAwards.stream().map(mfaAward ->
                new MFAAwardResponse(mfaAward)).collect(Collectors.toList());
        this.totalSearchResults = totalSearchResults;
        this.currentPage = currentPage;
        this.totalPages = totalPages;

        this.activeMfaAwards = mfaGroupingsCounts.get("activeMfaGroupings");
        this.deletedMfaAwards = mfaGroupingsCounts.get("deletedMfaGroupings");
        this.allMfaAwards = mfaGroupingsCounts.get("allMfaGroupings");
    }
}