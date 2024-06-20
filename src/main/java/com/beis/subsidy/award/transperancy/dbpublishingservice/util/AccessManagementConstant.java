package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

import java.util.Arrays;

public class AccessManagementConstant {
	public static String ACTIVE="Active";
	public static String INACTIVE="Inactive";
    public static String DELETED="Deleted";

    public static String AWAITING_APPROVAL ="Awaiting approval";
    public static String REJECTED ="Rejected";
    public static String PUBLISHED="Published";
    public static String BEIS_ADMIN_ROLE="BEIS Administrator";
    public static String ROLES[]= {"BEIS Administrator","Granting Authority Administrator","Granting Authority Approver","Granting Authority Encoder"};
    public static String ADMIN_ROLES[]= {"BEIS Administrator","Granting Authority Administrator"};
    public static String GA_ADMIN_ROLE="Granting Authority Administrator";
    public static String GA_APPROVER_ROLE="Granting Authority Approver";
    public static String GA_ENCODER_ROLE="Granting Authority Encoder";
    public static int GA_ALREADY_EXISTS = 491;

    public static String REGIONS[] = {"National", "UK-wide", "GB-wide", "England", "Northern Ireland", "Scotland", "Wales", "North East", "North West", "Yorkshire and Humber", "East Midlands", "West Midlands", "East of England", "London", "South East", "South West"};
    public static String REGIONS_LOWER[] = Arrays.stream(REGIONS).map(String::toLowerCase).toArray(String[]::new);
}