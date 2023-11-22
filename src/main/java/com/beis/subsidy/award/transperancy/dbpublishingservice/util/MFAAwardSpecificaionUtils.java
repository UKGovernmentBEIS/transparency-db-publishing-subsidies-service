package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAGrouping;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public final class MFAAwardSpecificaionUtils {
    public static Specification<MFAAward> mfaGroupingNameLike(String mfaGroupingName) {
        return (root, query, builder) -> {

            Join<MFAAward, MFAGrouping> groupingJoin = root.join("mfaGrouping", JoinType.LEFT);

            Predicate groupingNumberNullPredicate = builder.isNull(groupingJoin.get("mfaGroupingNumber"));
            Predicate hasGroupingFalsePredicate = builder.equal(root.get("mfaGroupingPresent"), false);

            Predicate nullCondition = builder.or(groupingNumberNullPredicate, hasGroupingFalsePredicate);

            Predicate likePredicate = builder.and(
                    builder.not(nullCondition),
                    builder.like(
                            builder.lower(root.get("mfaGrouping").get("mfaGroupingName")),
                            builder.lower(builder.literal("%" + mfaGroupingName.trim() + "%"))
                    )
            );

            return likePredicate;
        };
    }

    public static Specification<MFAAward> mfaAwardNumberEquals(String mfaAwardNumber) {
        long mfaAwardNumberLong;
        try {
            mfaAwardNumberLong = Long.parseLong(mfaAwardNumber.trim());
        } catch (NumberFormatException ignored){
            return null;
        }

        return (root, query, builder) -> builder.equal(root.get("mfaAwardNumber"), (mfaAwardNumberLong));
    }

    public static Specification<MFAAward> mfaGroupingByStatus(String status) {
        return (root, query, builder) -> builder.equal(builder.lower(root.get("status")),
                builder.lower(builder.literal(status.trim())));
    }

    public static Specification<MFAAward> grantingAuthorityNameEqual(String searchName) {
        return (root, query, builder) -> builder.equal(builder.lower(root.get("grantingAuthority").get("grantingAuthorityName")),
                builder.lower(builder.literal(searchName.trim())));
    }

    public static Specification<MFAAward> mfaGroupingNumberEquals(String mfaGroupingNumber) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("mfaGrouping").get("mfaGroupingNumber")),
                builder.lower(builder.literal("%" + mfaGroupingNumber.trim() + "%")));
    }
}
