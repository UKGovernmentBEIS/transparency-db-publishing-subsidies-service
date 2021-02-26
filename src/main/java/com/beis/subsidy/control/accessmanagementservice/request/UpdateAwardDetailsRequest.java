package com.beis.subsidy.control.accessmanagementservice.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateAwardDetailsRequest {

    private String subsidyControlTitle;
    private String nationalIdType;
    private String nationalId;
    private String beneficiaryName;
    private String orgSize;
    private String status;
    private String subsidyInstrument;
    private String subsidyObjective;
    private String subsidyAmountRange;
    private String subsidyAmountExact;
    private String legalGrantingDate;
    private String grantingAuthorityName;
    private String goodsOrServices;
    private String spendingRegion;
    private String spendingSector;
    private String reason;
}
