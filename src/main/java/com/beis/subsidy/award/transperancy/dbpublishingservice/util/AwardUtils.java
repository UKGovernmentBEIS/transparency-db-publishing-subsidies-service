package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
public class AwardUtils {

    public static boolean validateCompanyNumber(String companyNumber) {

        return companyNumber != null && companyNumber.length() == 8 && companyNumber.toLowerCase().matches("[a-z0-9]+");

    }

    public static String convertToObjectiveJSONString(String objectiveArrayString) {
        String[] objectiveSplit = (objectiveArrayString.split("\\s*\\|\\s*"));
        List<String> objectiveJson = new ArrayList<String>();
        for (String objective:objectiveSplit) {
            objective = '\"' + StringUtils.capitalize(objective) + '\"';
            objectiveJson.add(objective);
        }
        return "[" + String.join(",", objectiveJson) +"]";
    }
}
