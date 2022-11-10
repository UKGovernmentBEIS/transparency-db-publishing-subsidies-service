package com.beis.subsidy.award.transperancy.dbpublishingservice.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MFAAwardRequest {
    private String mfaAwardNumber;
    private boolean speiAssistance;
    private boolean mfaGroupingPresent;
    private String mfaGroupingId;
    private BigDecimal awardFullAmount;
    private String confirmationDate;
    private String grantingAuthorityName;
    private String beneficiaryName;
    private String nationalIdType;
    private String nationalIdNumber;
    private String status;
}