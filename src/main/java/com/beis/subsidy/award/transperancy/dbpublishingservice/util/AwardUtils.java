package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

public class AwardUtils {

    public static boolean validateCompanyNumber(String companyNumber) {

        return companyNumber != null && companyNumber.length() == 8 && companyNumber.toLowerCase().matches("[a-z0-9]+");

    }
}
