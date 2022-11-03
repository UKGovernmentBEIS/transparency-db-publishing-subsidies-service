package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.SearchUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MFAAwardResponse {

    @JsonProperty
    private String mfaAwardNumber;

    @JsonProperty
    private String createdBy;

    @JsonProperty
    private boolean isSpeiAssistance;

    @JsonProperty
    private String status;

    @JsonProperty
    private String grantingAuthorityName;

    @JsonProperty
    private String lastModifiedTimestamp;

    @JsonProperty
    private String createdTimestamp;

    @JsonProperty
    private String deletedTimestamp;

    @JsonProperty
    private String deletedBy;

    public MFAAwardResponse(MFAAward mfaAward){
        this.mfaAwardNumber = mfaAward.getMfaGroupingNumber();
        this.grantingAuthorityName = mfaAward.getGrantingAuthority().getGrantingAuthorityName();
        this.createdBy = mfaAward.getCreatedBy();
        this.status = mfaAward.getStatus();
        this.lastModifiedTimestamp = SearchUtils.dateTimeToFullMonthNameInDate(mfaAward.getLastModifiedTimestamp());
        this.createdTimestamp = SearchUtils.dateTimeToFullMonthNameInDate(mfaAward.getCreatedTimestamp());
        if(mfaAward.getDeletedBy() != null) {
            this.deletedBy = mfaAward.getDeletedBy();
        }
        if(mfaAward.getDeletedTimestamp() != null) {
            this.deletedTimestamp = SearchUtils.dateTimeToFullMonthNameInDate(mfaAward.getDeletedTimestamp());
        }
    }
}
