package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAGrouping;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.SearchUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MFAAwardResponse {

    @JsonProperty
    private long mfaAwardNumber;

    @JsonProperty
    private String isSpeiAssistance;

    @JsonProperty
    private boolean hasMfaGrouping;

    @JsonProperty
    private String mfaGroupingNumber;

    @JsonProperty
    private MFAGroupingResponse mfaGroupingResponse;

    @JsonProperty
    private String awardAmount;

    @JsonProperty
    private String confirmationDate;

    @JsonProperty
    private String grantingAuthorityName;

    @JsonProperty
    private String recipientName;

    @JsonProperty
    private String recipientIdType;

    @JsonProperty
    private String recipientIdNumber;

    @JsonProperty
    private String status;

    @JsonProperty
    private String createdBy;

    @JsonProperty
    private String approvedBy;

    @JsonProperty
    private String reason;

    @JsonProperty
    private String lastModifiedTimestamp;

    @JsonProperty
    private String createdTimestamp;

    @JsonProperty
    private String deletedBy;

    @JsonProperty
    private String deletedTimestamp;

    public MFAAwardResponse(MFAAward mfaAward){
        this.mfaAwardNumber = mfaAward.getMfaAwardNumber();
        this.isSpeiAssistance = mfaAward.isSPEI() ? "Yes" : "No";
        this.hasMfaGrouping = mfaAward.isMfaGroupingPresent();
        if (mfaAward.isMfaGroupingPresent()){
            this.mfaGroupingNumber = mfaAward.getMfaGroupingNumber();
            this.mfaGroupingResponse = new MFAGroupingResponse(mfaAward.getMfaGrouping());
        }
        this.awardAmount = SearchUtils.decimalNumberFormat(mfaAward.getAwardAmount());
        this.confirmationDate = SearchUtils.dateToFullMonthNameInDate(mfaAward.getConfirmationDate());
        this.grantingAuthorityName = mfaAward.getGrantingAuthority().getGrantingAuthorityName();
        this.recipientName = mfaAward.getRecipientName();
        this.recipientIdType = mfaAward.getRecipientIdType();
        this.recipientIdNumber = mfaAward.getRecipientId();
        this.status = mfaAward.getStatus();
        this.createdBy = mfaAward.getCreatedBy();
        this.approvedBy = mfaAward.getApprovedBy();
        this.reason = mfaAward.getReason();
        this.lastModifiedTimestamp = SearchUtils.dateTimeToFullMonthNameInDate(mfaAward.getLastModifiedTimestamp());
        this.createdTimestamp = SearchUtils.dateTimeToFullMonthNameInDate(mfaAward.getCreatedTimestamp());
        this.deletedBy = mfaAward.getDeletedBy();
        if(mfaAward.getDeletedTimestamp() != null) {
            this.deletedTimestamp = SearchUtils.dateTimeToFullMonthNameInDate(mfaAward.getDeletedTimestamp());
        }
    }
}
