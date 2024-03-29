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
    private long publishedMfaAwards;

    private long awaitingMfaAwards;
    private long rejectedMfaAwards;

    private long deletedMfaAwards;
    private long allMfaAwards;
    public long totalSearchResults;
    public int currentPage;
    public int totalPages;
    @JsonProperty
    public List<MFAAwardResponse> mfaAwards;

    public MFAAwardsResponse(List<MFAAward> mfaAwards, long totalSearchResults,
                             int currentPage, int totalPages, Map<String, Long> mfaAwardCounts) {
        this.mfaAwards = mfaAwards.stream().map(mfaAward ->
                new MFAAwardResponse(mfaAward)).collect(Collectors.toList());
        this.totalSearchResults = totalSearchResults;
        this.currentPage = currentPage;
        this.totalPages = totalPages;

        this.publishedMfaAwards = mfaAwardCounts.get("publishedMfaAwards");
        this.awaitingMfaAwards = mfaAwardCounts.get("awaitingMfaAwards");
        this.rejectedMfaAwards = mfaAwardCounts.get("rejectedMfaAwards");
        this.deletedMfaAwards = mfaAwardCounts.get("deletedMfaAwards");
        this.allMfaAwards = mfaAwardCounts.get("allMfaAwards");
    }
}