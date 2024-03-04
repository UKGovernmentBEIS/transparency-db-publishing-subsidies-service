package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

public class AwardUtils {

    public static boolean validateCompanyNumber(String companyNumber) {

        if(companyNumber!=null && (companyNumber.length()!=8 || !companyNumber.matches("[A-Za-z0-9]+"))) {
            return true;
        }
        return false;
    }
}
