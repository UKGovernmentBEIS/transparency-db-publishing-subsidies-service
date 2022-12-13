package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

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
public class MFAGroupingsResponse {
    private long activeMfaGroupings;
    private long deletedMfaGroupings;
    private long allMfaGroupings;
    public long totalSearchResults;
    public int currentPage;
    public int totalPages;
    @JsonProperty
    public List<MFAGroupingResponse> mfaGroupings;

    public MFAGroupingsResponse(List<MFAGrouping> mfaGroupings, long totalSearchResults,
                                int currentPage, int totalPages, Map<String, Long> mfaGroupingsCounts) {
        this.mfaGroupings = mfaGroupings.stream().map(mfaGrouping ->
                new MFAGroupingResponse(mfaGrouping)).collect(Collectors.toList());
        this.totalSearchResults = totalSearchResults;
        this.currentPage = currentPage;
        this.totalPages = totalPages;

        this.activeMfaGroupings = mfaGroupingsCounts.get("activeMfaGroupings");
        this.deletedMfaGroupings = mfaGroupingsCounts.get("deletedMfaGroupings");
        this.allMfaGroupings = mfaGroupingsCounts.get("allMfaGroupings");
    }
}