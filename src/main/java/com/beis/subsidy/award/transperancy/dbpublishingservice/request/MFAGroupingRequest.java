package com.beis.subsidy.award.transperancy.dbpublishingservice.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MFAGroupingRequest {
    private String mfaGroupingNumber;
    private String mfaGroupingName;
    private String grantingAuthorityName;
    private String status;
}
