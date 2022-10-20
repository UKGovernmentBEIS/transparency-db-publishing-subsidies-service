package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAGrouping;
import org.springframework.data.jpa.domain.Specification;

public final class MFAGroupingSpecificaionUtils {
    public static Specification<MFAGrouping> mfaGroupingNameLike(String mfaGroupingName) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("mfaGroupingName")),
                builder.lower(builder.literal("%" + mfaGroupingName.trim() + "%")));
    }

    public static Specification<MFAGrouping> mfaGroupingNumberEquals(String mfaGroupingNumber) {
        return (root, query, builder) -> builder.equal(builder.lower(root.get("mfaGroupingNumber")),
                builder.lower(builder.literal(mfaGroupingNumber.trim())));
    }

    public static Specification<MFAGrouping> mfaGroupingByStatus(String status) {
        return (root, query, builder) -> builder.equal(builder.lower(root.get("status")),
                builder.lower(builder.literal(status.trim())));
    }

    public static Specification<MFAGrouping> grantingAuthorityName(String searchName) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("grantingAuthority").get("grantingAuthorityName")),
                builder.lower(builder.literal("%" + searchName.trim() + "%")));
    }
}
