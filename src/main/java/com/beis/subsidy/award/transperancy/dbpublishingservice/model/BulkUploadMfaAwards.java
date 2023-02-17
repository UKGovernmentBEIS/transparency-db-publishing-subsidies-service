package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkUploadMfaAwards {

    private int row;
    private String column;
    private boolean speiaAward;
    private boolean mfaSpeiaGrouping;
    private String groupingId;
    private BigDecimal awardFullAmount;
    private LocalDate confirmationDate;
    private String publicAuthority;
    private String orgName;
    private String orgIdType;
    private String idNumber;

    public Boolean getMfaSpeiaGrouping() {
        return mfaSpeiaGrouping;
    }

    public Boolean getSpeiaAward() {
        return speiaAward;
    }
}
