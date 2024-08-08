package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.UserPrinciple;
import com.beis.subsidy.award.transperancy.dbpublishingservice.exception.UnauthorisedAccessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 
 * Search Utility class 
 */
@Slf4j
public class SearchUtils {

	/**
	 * To check if input string is null or empty
	 *
	 * @param inputString - input string
	 * @return boolean - true or false
	 */
	public static boolean checkNullOrEmptyString(String inputString) {
		return inputString == null || inputString.trim().isEmpty();
	}

	/**
	 * To convert string date in format YYYY-MM-DD to LocalDate (without timezone)
	 *
	 * @param inputStringDate - input string date
	 * @return
	 */
	public static LocalDate stringToDate(String inputStringDate) {
		return LocalDate.parse(inputStringDate);
	}

	/**
	 * To convert string date in format YYYY-MM-DD to DD FullMONTHNAME YYYY
	 *
	 * @param inputStringDate - input string date
	 * @return
	 */
	public static String dateToFullMonthNameInDate(LocalDate inputStringDate) {
		if (!(inputStringDate == null)) {
			log.info("input Date ::{}", inputStringDate);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy");
			return dateFormat.format(inputStringDate);
		} else {
			return null;
		}
	}

	public static String timestampToFullMonthNameInDate(Date inputDateObj) {
		log.info("input Date ::{}", inputDateObj);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
		return dateFormat.format(inputDateObj);
	}

	/**
	 * To convert Local DateTime to DD FullMONTHNAME YYYY
	 *
	 * @param inputDateTime - input string date
	 * @return
	 */
	public static String dateTimeToFullMonthNameInDate(LocalDateTime inputDateTime) {
		log.info("input Date ::{}", inputDateTime);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy H:mm:ss");
		return dateFormat.format(inputDateTime);
	}

	/**
	 * To convert BigDecimal to string by adding , for thousands.
	 *
	 * @param subsidyFullAmountExact
	 * @return
	 */
	public static String decimalNumberFormat(BigDecimal subsidyFullAmountExact) {
		DecimalFormat numberFormat = new DecimalFormat("###,###.##");
		return numberFormat.format(subsidyFullAmountExact.longValue());
	}

	/**
	 * To convert Amount Range to by adding pound and , for thousands.
	 *
	 * @param amountRange
	 * @return formatted string
	 */
	public static String formatedFullAmountRange(String amountRange) {
		String finalAmtRange = "NA";
		if (StringUtils.isNotBlank(amountRange) &&
				!(amountRange.equalsIgnoreCase("NA") || amountRange.contains("N/A")
						|| amountRange.contains("n/a"))
		&& !amountRange.endsWith("or more")) {

			StringBuilder format = new StringBuilder();
			String[] tokens = amountRange.split("-");
			if (tokens.length == 2) {
				finalAmtRange = format.append(convertDecimalValue(tokens[0]))
						.append(" - ")
						.append("£")
						.append(decimalNumberFormat(new BigDecimal(tokens[1].trim()))).toString();
			} else 	{
					finalAmtRange = new BigDecimal(amountRange).longValue() > 0 ? format.append("£")
							.append(decimalNumberFormat(new BigDecimal(amountRange))).toString() : "0";
			}

		} else if(StringUtils.isNotBlank(amountRange) && amountRange.endsWith("or more")) {
			String removedLessThanVal = amountRange.substring(0, amountRange.length()-7).trim();
			finalAmtRange = "£"  + decimalNumberFormat(new BigDecimal(removedLessThanVal)) + " or more";
		}
		return finalAmtRange;
	}

	public static String convertDecimalValue(String token) {
		String formatNumber = "";
		if (!token.contains("NA/na")) {
			String removedLessThanVal = token.contains(">") ? token.substring(1, token.length()).trim() : token.trim();
			formatNumber = decimalNumberFormat(new BigDecimal(removedLessThanVal));
			if (token.contains(">")) {
				formatNumber = ">£" + formatNumber;
			} else {
				formatNumber = "£" + formatNumber;
			}
		}
		return formatNumber;
	}

	final static String DATE_FORMAT = "yyyy-MM-dd";

	public static boolean isDateValid(String date)
	{
		try {
			DateFormat df = new SimpleDateFormat(DATE_FORMAT);
			df.setLenient(false);
			df.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	public static UserPrinciple isAllRolesValidation(ObjectMapper objectMapper, HttpHeaders userPrinciple, String entity) {
		UserPrinciple userPrincipleObj = null;
		String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
		try {
			userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
			if (!Arrays.asList(AccessManagementConstant.ROLES).contains(userPrincipleObj.getRole())) {
				throw new UnauthorisedAccessException("You are not authorised to "+ entity);
			}
		} catch(JsonProcessingException exception){
			throw new UnauthorisedAccessException("Unauthorised exception");
		}
		return userPrincipleObj;
	}

	public static List<Sort.Order> getOrderByCondition(String[] sortBy) {
		List<Sort.Order> orders = new ArrayList<Sort.Order>();
		if (sortBy != null && sortBy.length > 0 && sortBy[0].contains(",")) {
			for (String sortOrder : sortBy) {
				String[] _sort = sortOrder.split(",");
				orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
			}
		} else {
			orders.add(new Sort.Order(getSortDirection("desc"), "lastModifiedTimestamp"));
		}
		return orders;
	}

	private static Sort.Direction getSortDirection(String direction) {
		Sort.Direction sortDir = Sort.Direction.ASC;
		if (direction.equals("desc")) {
			sortDir = Sort.Direction.DESC;
		}
		return sortDir;
	}
}
