package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.UserPrinciple;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.AuditLogs;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadMfaAwards;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AuditLogsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.expression.ParseException;
import org.springframework.web.multipart.MultipartFile;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelHelper {

	public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	public final static int EXPECTED_COLUMN_COUNT = 24;
	
	public final static int EXPECTED_MFA_COLUMN_COUNT = 9;

	public  final static String SHEET = "Upload Template";

	/**
	 * Method to check excel file format to be xlsx
	 * @param file
	 * @return
	 */
	public static boolean hasExcelFormat(MultipartFile file) {
		boolean flag = true;
		if(!TYPE.equals(file.getContentType())) {
			flag = false;
		}
		log.info("Inside ExcelHelper hasExcelFormat ::{}", flag);
		return flag;
	}



	public static List < BulkUploadAwards > excelToAwards(InputStream is) {
		try {
			log.info("Inside excelToAwards::DBPublishingSubsidies Service");
			Workbook workbook = new XSSFWorkbook(is);

			Sheet sheet = workbook.getSheet(SHEET);
			Iterator < Row > rows = sheet.iterator();

			log.info("first row " + sheet.getFirstRowNum());
			List < BulkUploadAwards > bulkUploadAwardsList = new ArrayList < BulkUploadAwards > ();
			log.info("last row " + sheet.getLastRowNum());
			int rowNumber = 0;
			while (rows.hasNext()) {
				log.info("before rows.next");
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}
				if (containsValue(currentRow)) {
					log.info("BulkUploadAwardsController Going Inside switch block", rowNumber);

					BulkUploadAwards bulkUploadAwards = new BulkUploadAwards();
					bulkUploadAwards.setRow(currentRow.getRowNum() + 1);

					for (int i = 0; i < currentRow.getLastCellNum(); i++) {
						Cell currentCell = currentRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

						switch (i) {
							case 0:
								bulkUploadAwards.setSubsidyControlNumber(currentCell.getStringCellValue().trim());
								break;
							case 1:
								bulkUploadAwards.setSubsidyControlTitle(currentCell.getStringCellValue().trim());
								break;
							case 2:
								bulkUploadAwards.setAdminProgramNumber(currentCell.getStringCellValue().trim());
								break;
							case 3:
								bulkUploadAwards.setStandaloneAward(currentCell.getStringCellValue().trim());
								break;
							case 4:
								if(bulkUploadAwards.getStandaloneAward().equalsIgnoreCase("yes"))
									bulkUploadAwards.setSubsidyAwardInterest(currentCell.getStringCellValue().trim());
								break;
							case 5:
								if(bulkUploadAwards.getStandaloneAward().equalsIgnoreCase("yes")) {
									if (currentCell.getCellType() == CellType.BLANK) {
										bulkUploadAwards.setSpecificPolicyObjective(null);
									} else {
										bulkUploadAwards.setSpecificPolicyObjective(currentCell.getStringCellValue().trim());
									}
								}
								break;
							case 6:
								bulkUploadAwards.setSubsidyDescription(currentCell.getStringCellValue().trim());
								break;
							case 7:
								if(bulkUploadAwards.getStandaloneAward().equalsIgnoreCase("yes"))
									bulkUploadAwards.setAuthorityURL(currentCell.getStringCellValue());
								break;
							case 8:
								if(bulkUploadAwards.getStandaloneAward().equalsIgnoreCase("yes"))
									bulkUploadAwards.setAuthorityURLDescription(currentCell.getStringCellValue());
								break;
							case 9:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setSubsidyObjective(null);
								} else {
									bulkUploadAwards.setSubsidyObjective(currentCell.getStringCellValue().trim());
								}
								break;
							case 10:
								if (currentCell.getCellType()==CellType.BLANK || currentCell.getStringCellValue().trim().isEmpty()) {
									bulkUploadAwards.setSubsidyObjectiveOther(null);
									//if purpose other is populated but purpose is blank
								}else if(currentCell.getCellType()!=CellType.BLANK && bulkUploadAwards.getSubsidyObjective() == null) {
									bulkUploadAwards.setSubsidyObjective("Other - " + currentCell.getStringCellValue().trim());
									bulkUploadAwards.setSubsidyObjectiveOther(currentCell.getStringCellValue().trim());
									//if purpose and other purpose are both populated
								}else {
									bulkUploadAwards.setSubsidyObjectiveOther(currentCell.getStringCellValue().trim());
									bulkUploadAwards.setSubsidyObjective(bulkUploadAwards.getSubsidyObjective().replace("Other", "Other - " + currentCell.getStringCellValue().replace("Other - ", "").trim()));
								}
								break;
							case 11:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setSubsidyInstrument(null);
								} else {
									bulkUploadAwards.setSubsidyInstrument(currentCell.getStringCellValue().trim());
								}
								break;
							case 12:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setSubsidyInstrumentOther(null);
								} else {
									bulkUploadAwards.setSubsidyInstrumentOther(currentCell.getStringCellValue().trim());
								}
								break;
							case 13:
								bulkUploadAwards.setSubsidyAmountRange((currentCell == null || currentCell.getCellType() == CellType.BLANK || (currentCell.getCellType().equals(CellType.STRING) && currentCell.getStringCellValue().trim().isEmpty())) ? null : currentCell.getStringCellValue().trim());
								break;
							case 14:
								if (currentCell.getCellType() == CellType.STRING) {
									bulkUploadAwards.setSubsidyAmountExact((currentCell.getStringCellValue().trim()));
								} else if (currentCell.getCellType() == CellType.NUMERIC) {
									bulkUploadAwards.setSubsidyAmountExact((String.valueOf(currentCell.getNumericCellValue())));
								}
								break;
							case 15:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setNationalIdType(null);
								} else {
									bulkUploadAwards.setNationalIdType(currentCell.getStringCellValue().trim());
								}
								break;
							case 16:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setNationalId(null);
								} else {
									bulkUploadAwards.setNationalId(((currentCell.getCellType() == CellType.NUMERIC) ?
											String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).longValue()) :
											currentCell.getStringCellValue().trim()));
								}
								break;
							case 17:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setBeneficiaryName(null);
								} else {
									bulkUploadAwards.setBeneficiaryName(currentCell.getStringCellValue().trim());
								}
								break;
							case 18:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setOrgSize(null);
								} else {
									bulkUploadAwards.setOrgSize(currentCell.getStringCellValue().trim());
								}

								break;
							case 19:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setGrantingAuthorityName(null);
								} else {
									bulkUploadAwards.setGrantingAuthorityName(currentCell.getStringCellValue().trim());
								}
								break;
							case 20:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setLegalGrantingDate(null);
								}
								if (currentCell.getCellType() == CellType.STRING) {
									bulkUploadAwards.setLegalGrantingDate("invalid");
								} else {
									bulkUploadAwards.setLegalGrantingDate(convertDateToString(currentCell.getDateCellValue()));
								}
								break;
							case 21:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setGoodsOrServices(null);
								} else {
									bulkUploadAwards.setGoodsOrServices(currentCell.getStringCellValue().trim());
								}
								break;
							case 22:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setSpendingRegion(null);
								} else {
									bulkUploadAwards.setSpendingRegion(currentCell.getStringCellValue().trim());
								}
								break;
							case 23:
								if (currentCell.getCellType() == CellType.BLANK) {
									bulkUploadAwards.setSpendingSector(null);
								} else {
									bulkUploadAwards.setSpendingSector(currentCell.getStringCellValue().trim());
								}
								break;
							default:
								break;
						}
					}

					bulkUploadAwardsList.add(bulkUploadAwards);
				} else {
					break;
				}
			}

			workbook.close();

			log.info("Excel - List - size = " + bulkUploadAwardsList.size());
			return bulkUploadAwardsList;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {

			throw new RuntimeException("fail to read Excel file: " + e);
		}
	}

	public static List<BulkUploadMfaAwards> excelToMfaAwards(InputStream is) {
		try {

			log.info("Inside excelToMfaAwards::DBPublishingSubsidies Service" );

			Workbook workbook = new XSSFWorkbook(is);

			Sheet sheet = workbook.getSheet(SHEET);
			Iterator<Row> rows = sheet.iterator();

			log.info("first row " + sheet.getFirstRowNum());
			List<BulkUploadMfaAwards> bulkUploadMfaAwardsList = new ArrayList<BulkUploadMfaAwards>();
			log.info("last row " + sheet.getLastRowNum());
			int rowNumber = 0;
			DataFormatter formatter = new DataFormatter();
			while (rows.hasNext()) {
				log.info("before rows.next");
				Row currentRow = rows.next();


				// skip header
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}
				if (containsValue(currentRow)) {
					log.info("BulkUploadAwardsController Going Inside switch block" ,rowNumber);
					Iterator<Cell> cellsInRow = currentRow.iterator();

					BulkUploadMfaAwards bulkUploadMfaAwards = new BulkUploadMfaAwards();
					bulkUploadMfaAwards.setRow(currentRow.getRowNum() + 1);

					int cellIdx = 0;

					while (cellsInRow.hasNext()) {
						Cell currentCell = cellsInRow.next();

						switch (cellIdx) {

							case 0:
								if(currentCell.getStringCellValue().equalsIgnoreCase("yes")){
									bulkUploadMfaAwards.setSpeiaAward(true);
								}else if(currentCell.getStringCellValue().equalsIgnoreCase("no")){
									bulkUploadMfaAwards.setSpeiaAward(false);
								}else if(currentCell.getCellType()==CellType.BLANK){
									bulkUploadMfaAwards.setSpeiaAward(null);
								}
								break;

							case 1:
								if(currentCell.getStringCellValue().equalsIgnoreCase("yes")){
									bulkUploadMfaAwards.setMfaSpeiaGrouping(true);
								}else if(currentCell.getStringCellValue().equalsIgnoreCase("no")){
									bulkUploadMfaAwards.setMfaSpeiaGrouping(false);
								}else if(currentCell.getCellType()==CellType.BLANK){
									bulkUploadMfaAwards.setMfaSpeiaGrouping(null);
								}

								break;

							case 2:
								if(currentCell.getCellType()==CellType.BLANK) {
									bulkUploadMfaAwards.setGroupingId(null);
								}else {
									bulkUploadMfaAwards.setGroupingId(currentCell.getStringCellValue().trim());
								}

								break;

							case 3:
								if(currentCell.getCellType()==CellType.STRING) {
									bulkUploadMfaAwards.setAwardFullAmount(currentCell.getStringCellValue().trim());
								} else if (currentCell.getCellType()==CellType.BLANK) {
									bulkUploadMfaAwards.setAwardFullAmount("");
								} else {
									bulkUploadMfaAwards.setAwardFullAmount(String.valueOf(currentCell.getNumericCellValue()));
								}

								break;

							case 4:
								if(currentCell.getCellType()==CellType.BLANK) {
									bulkUploadMfaAwards.setConfirmationDate(null);
								}
								if(currentCell.getCellType()==CellType.STRING) {
									bulkUploadMfaAwards.setConfirmationDate(null);

								}else {
									bulkUploadMfaAwards.setConfirmationDate(convertDateToLocalDate(currentCell.getDateCellValue()));
								}
								break;

							case 5:
								bulkUploadMfaAwards.setPublicAuthority(currentCell.getStringCellValue().trim());

								break;

							case 6:
								bulkUploadMfaAwards.setOrgName(currentCell.getStringCellValue().trim());

								break;

							case 7:
								bulkUploadMfaAwards.setOrgIdType(currentCell.getStringCellValue().trim());

								break;

							case 8:
								bulkUploadMfaAwards.setIdNumber(formatter.formatCellValue(currentCell).trim());

								break;

							default:
								break;
						}

						cellIdx++;
					}


					bulkUploadMfaAwardsList.add(bulkUploadMfaAwards);
				}else {
					break;
				}
			}

			workbook.close();

			log.info("Excel - List - size = " + bulkUploadMfaAwardsList.size());
			return bulkUploadMfaAwardsList;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (Exception e) {

			throw new RuntimeException("fail to read Excel file: " + e);
		}
	}

	private static String convertDateToString(Date incomingDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		String date = null ;
		if(incomingDate!=null) {
			try {
				date = formatter.format(incomingDate).toString();
			} catch (ParseException e) {
				log.error("Error while converting the date inside convertDateToString",e);
				return date;
			}
		}
	return date;
	}

	public static boolean containsValue(Row row){
		StringBuilder data = new StringBuilder();
		DataFormatter formatter = new DataFormatter();
		Iterator<Cell> iterator = row.cellIterator();

		while(iterator.hasNext()){
			Cell currentCell = iterator.next();
			data.append(formatter.formatCellValue(currentCell));
		}

		if(StringUtils.isEmpty(data)){
			return false;
		}else{
			return true;
		}
	}

	public static void saveAuditLog(UserPrinciple userPrinciple, String action,String role,
									AuditLogsRepository auditLogsRepository) {
		AuditLogs audit = new AuditLogs();
		try {
			String status ="Published";

			if ("Granting Authority Encoder".equals(role.trim())) {
				status = "Awaiting Approval";
			}
			StringBuilder msg = new StringBuilder("Award ")
					.append(" added with status ").append(status) ;

			String userName = userPrinciple.getUserName();
			audit.setUserName(userName);
			audit.setEventType(action);
			audit.setEventId(role);
			audit.setEventMessage(msg.toString());
			audit.setGaName(userPrinciple.getGrantingAuthorityGroupName());
			audit.setCreatedTimestamp(LocalDate.now());
			auditLogsRepository.save(audit);
		} catch(Exception e) {
			log.error("{} :: saveAuditLog failed to perform action", e);
		}
	}

	public static void saveAuditLogForUpdate(UserPrinciple userPrinciple, String action,String awardNo, String eventMsg,
									AuditLogsRepository auditLogsRepository) {
		AuditLogs audit = new AuditLogs();
		try {
			String userName = userPrinciple.getUserName();
			audit.setUserName(userName);
			audit.setEventType(action);
			audit.setEventId(awardNo);
			audit.setEventMessage(eventMsg.toString());
			audit.setGaName(userPrinciple.getGrantingAuthorityGroupName());
			audit.setCreatedTimestamp(LocalDate.now());
			auditLogsRepository.save(audit);
		} catch(Exception e) {
			log.error("{} :: saveAuditLogForUpdate failed to perform action", e);
		}
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static Boolean validateColumnCount(InputStream is) {
		Workbook workbook = null;
		try {
			workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);

			int headerColumnCount = sheet.getRow(0).getLastCellNum();
			if (headerColumnCount == EXPECTED_COLUMN_COUNT){
				return true;
			}
		} catch (IOException e) {
			throw new RuntimeException("fail to read Excel file: " + e);
		}
		return false;
	}

	public static Boolean validateMfaColumnCount(InputStream is) {
		Workbook workbook = null;
		try {
			workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);

			int headerColumnCount = sheet.getRow(0).getLastCellNum();
			if (headerColumnCount == EXPECTED_MFA_COLUMN_COUNT){
				return true;
			}
		} catch (IOException e) {
			throw new RuntimeException("fail to read Excel file: " + e);
		}
		return false;
	}

	private static LocalDate convertDateToLocalDate(Date incomingDate){
		return incomingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
