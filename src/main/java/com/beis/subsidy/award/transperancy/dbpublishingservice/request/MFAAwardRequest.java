package com.beis.subsidy.award.transperancy.dbpublishingservice.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MFAAwardRequest {
    private String mfaAwardNumber;
    private boolean isSpeiAssistance;
    private String mfaGroupingId;
    private String beneficiaryName;
    private String beneficiaryId;
    private String grantingAuthorityName;
    private LocalDate createdTimestamp;
    private String status;
}