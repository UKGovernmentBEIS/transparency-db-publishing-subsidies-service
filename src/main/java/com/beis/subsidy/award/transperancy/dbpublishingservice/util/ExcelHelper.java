package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.expression.ParseException;
import org.springframework.web.multipart.MultipartFile;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelHelper {
	
	public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	
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

	
	
	public static List<BulkUploadAwards> excelToAwards(InputStream is) {
	    try {
	       	    	
	    	log.info("ïnside excelToAwards::DBPublishingSubsidies Service" );
	        
	        Workbook workbook = new XSSFWorkbook(is);

	        Sheet sheet = workbook.getSheet(SHEET);
			Iterator<Row> rows = sheet.iterator();

			log.info("first row " + sheet.getFirstRowNum());
			List<BulkUploadAwards> bulkUploadAwardsList = new ArrayList<BulkUploadAwards>();
			log.info("last row " + sheet.getLastRowNum());
			int rowNumber = 0;
			  while (rows.hasNext()) {
				  log.info("before rows.next");
				Row currentRow = rows.next();

				log.info("BulkUploadAwardsController Going Inside switch block");
				// skip header
				if (rowNumber == 0) {
				  rowNumber++;
				  continue;
				}
				if (containsValue(currentRow)) {

				Iterator<Cell> cellsInRow = currentRow.iterator();

				BulkUploadAwards bulkUploadAwards = new BulkUploadAwards();
				bulkUploadAwards.setRow(currentRow.getRowNum() + 1);

				int cellIdx = 0;
				while (cellsInRow.hasNext()) {
				  Cell currentCell = cellsInRow.next();

				  switch (cellIdx) {

				  case 0:
					  bulkUploadAwards.setSubsidyControlNumber(currentCell.getStringCellValue());

					break;

				  case 1:
					  bulkUploadAwards.setSubsidyControlTitle(currentCell.getStringCellValue());

					break;

				  case 2:
					 if(currentCell.getCellType()==CellType.BLANK) {
						 bulkUploadAwards.setSubsidyObjective(null);
					 }else {
					  bulkUploadAwards.setSubsidyObjective(currentCell.getStringCellValue());
					 }

					break;

				  case 3:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setSubsidyObjectiveOther(null);
					  }else {
					  bulkUploadAwards.setSubsidyObjectiveOther(currentCell.getStringCellValue());
					  }

					break;

				  case 4:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setSubsidyInstrument(null);
					  }else {
					  bulkUploadAwards.setSubsidyInstrument(currentCell.getStringCellValue());
					  }
					  break;

				  case 5:
					  if(currentCell.getCellType()==CellType.BLANK) {	
					  bulkUploadAwards.setSubsidyInstrumentOther(null);
					  }else {
						  bulkUploadAwards.setSubsidyInstrumentOther(currentCell.getStringCellValue());
					  }

					  break;

				  case 6:
					  //bulkUploadAwards.setOrgSize(currentCell.getStringCellValue());
					  bulkUploadAwards.setSubsidyAmountRange( (currentCell == null || currentCell.getCellType() == CellType.BLANK || (currentCell.getCellType().equals(CellType.STRING) && currentCell.getStringCellValue().trim().isEmpty())) ? null : currentCell.getStringCellValue() );

					break;

				  case 7:
					  if (currentCell.getCellTypeEnum() == CellType.STRING) {
					  bulkUploadAwards.setSubsidyAmountExact((currentCell.getStringCellValue()));
					  }else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
						 bulkUploadAwards.setSubsidyAmountExact((String.valueOf(currentCell.getNumericCellValue())));
					  }

					  break;

				  case 8:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setNationalIdType(null);
					  }else {
						  bulkUploadAwards.setNationalIdType(currentCell.getStringCellValue());
					  }

					break;

				  case 9:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setNationalId(null);
					  }else {
					  bulkUploadAwards.setNationalId( ((currentCell.getCellType().getCode() == CellType.NUMERIC.getCode()) ?  String.valueOf(Double.valueOf( currentCell.getNumericCellValue()).longValue()) : currentCell.getStringCellValue()));
					  }

					break;

				  case 10:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setBeneficiaryName(null);
					  }else {
						  bulkUploadAwards.setBeneficiaryName(currentCell.getStringCellValue());
					  }

					break;

				  case 11:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setOrgSize(null);
					  }else {
					  bulkUploadAwards.setOrgSize(currentCell.getStringCellValue());
					  }

					break;

				  case 12:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setGrantingAuthorityName(null);
					  }else {
					  bulkUploadAwards.setGrantingAuthorityName(currentCell.getStringCellValue());
					  }

					break;

				  case 13:
					 
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setLegalGrantingDate(null);
					  }
					  if(currentCell.getCellType()==CellType.STRING) {
						  bulkUploadAwards.setLegalGrantingDate("invalid");
						  
					  }else {
					  bulkUploadAwards.setLegalGrantingDate(convertDateToString(currentCell.getDateCellValue()));
					  }

					break;

				  case 14:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setGoodsOrServices(null);
					  }else {
					  bulkUploadAwards.setGoodsOrServices(currentCell.getStringCellValue());
					  }

					break;

				  case 15:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setSpendingRegion(null);
					  }else {
					  bulkUploadAwards.setSpendingRegion(currentCell.getStringCellValue());
					  }

					break;

				  case 16:
					  if(currentCell.getCellType()==CellType.BLANK) {
						  bulkUploadAwards.setSpendingSector(null);
					  }else {
					  bulkUploadAwards.setSpendingSector(currentCell.getStringCellValue());
					  }

					break;

				  default:
					break;
				  }

				  cellIdx++;
				}


				bulkUploadAwardsList.add(bulkUploadAwards);
				}else {
					break;
				}
	      }

	      workbook.close();

	      log.info("Excel - List - size = " + bulkUploadAwardsList.size());
	      return bulkUploadAwardsList;
	    } catch (IOException e) {
	      log.info("fail to parse Excel file: " + e.getMessage());
	      throw new RuntimeException(e);
	    }
	    catch (Exception e) {
	    	log.info("fail to read Excel file: " + e.getMessage());
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
				log.error("Error while converting the date inside convertDateToString");
				return date;
			}
		}
	return date;
	}
	
	public static boolean containsValue(Row row)
	{
	    boolean flag = true;
	   if(row.getCell(0).getCellType()==CellType.BLANK && row.getCell(1).getCellType()==CellType.BLANK && row.getCell(2).getCellType()==CellType.BLANK) {
		   return false;
	   }
	    if ((StringUtils.isEmpty(String.valueOf(row.getCell(0))) == true && 
	    		StringUtils.isEmpty(String.valueOf(row.getCell(1))) == true && StringUtils.isEmpty(String.valueOf(row.getCell(2))) == true  ) ||
	    (String.valueOf(row.getCell(0))==null && String.valueOf(row.getCell(1))==null && String.valueOf(row.getCell(2))==null ) && row.getCell(2).getCellType()==CellType.BLANK)
	    {
			flag = false;
	    }
	 	return flag;
	}
}
