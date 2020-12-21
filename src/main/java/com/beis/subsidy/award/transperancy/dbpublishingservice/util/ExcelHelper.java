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
	
	
	public  static String[] HEADERs = { 
			"Subsidy Control Number",
			"Subsidy Measure Title",
			"Subsidy Objective",
			"Subsidy Objective \"Other\" - Text",
			"Subsidy instrument",
			"Subsidy Instrument \"Other\" - Text",
			"Subsidy Element Full amount (£)",
			"Subsidy Element Full amount Range",
			"National ID Type",
			"National ID Number",
			"Beneficiary Name",
			"Size of the Organisation",
			"Granting Authority Name",
			"Legal Granting Date",
			"Goods or Services",
			"Spending Region",
			"Spending Sector",
	};
	
	public  static String SHEET = "Upload Template";

	/**
	 * Method to check excel file format to be xlsx
	 * @param file
	 * @return
	 */
	public static boolean hasExcelFormat(MultipartFile file) {

		if(!TYPE.equals(file.getContentType())) {
			return false;
		}
		log.info("hasExcelFormat true BulkUploadAwardsController");
		return true;
	}

	
	
	public static List<BulkUploadAwards> excelToAwards(InputStream is) {
	    try {
	       // List<Test> tempStudentList = new ArrayList<Test>();
	        //XSSFWorkbook workbook = new XSSFWorkbook(reapExcelDataFile.getInputStream());
	        //XSSFSheet worksheet = workbook.getSheetAt(0);
	    	
	    	log.info("ïnside excelToAwards::DBPublishingSubsideies Service" );
	        
	        Workbook workbook = new XSSFWorkbook(is);

	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      log.info("first row "+sheet.getFirstRowNum());
	      List<BulkUploadAwards> bulkUploadAwardsList = new ArrayList<BulkUploadAwards>();
	      log.info("last row "+sheet.getLastRowNum());
	      int rowNumber = 0;
	      while (rows.hasNext()) {
	        Row currentRow = rows.next();

	        log.info("BulkUploadAwardsController Going Inside switch block");
	        // skip header
	        if (rowNumber == 0) {
	          rowNumber++;
	          continue;
	        }
	        int fcell = currentRow.getFirstCellNum();// first cell number of excel
	        int lcell = currentRow.getLastCellNum();
	        if(containsValue(currentRow,fcell,lcell)) {
	        	log.info("am inside contains");
	        	
	        
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
	        	  //System.out.println(bulkUploadAwards);
	            break;

	          case 2:
	        	 if(currentCell.getCellType()==CellType.BLANK) {
	        		 bulkUploadAwards.setSubsidyObjective(null);
	        	 }else {
	        	  bulkUploadAwards.setSubsidyObjective(currentCell.getStringCellValue());
	        	 }
	        	  //log.info(bulkUploadAwards);
	            break;
	            
	          case 3:
		        	 
	        	  bulkUploadAwards.setSubsidyObjectiveOther(currentCell.getStringCellValue());
	        	  //System.out.println(bulkUploadAwards);
	            break;

	          case 4:
	        	  if(currentCell.getCellType()==CellType.BLANK) {
	        		  bulkUploadAwards.setSubsidyInstrument(null);
	        	  }else {
	        	  bulkUploadAwards.setSubsidyInstrument(currentCell.getStringCellValue());
	        	  }
	        	  break;
	            
	          case 5:
	        	  	
	        	  bulkUploadAwards.setSubsidyInstrumentOther(currentCell.getStringCellValue());	
	        	 // log.info(bulkUploadAwards);
	        	  break;
	        	            
	          case 6:
	        	  //bulkUploadAwards.setOrgSize(currentCell.getStringCellValue());
	        	  bulkUploadAwards.setSubsidyAmountRange( (currentCell == null || currentCell.getCellType() == CellType.BLANK || (currentCell.getCellType().equals(CellType.STRING) && currentCell.getStringCellValue().trim().isEmpty())) ? null : currentCell.getStringCellValue() );
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 7:
	        	  if (currentCell.getCellTypeEnum() == CellType.STRING) {
	        	  bulkUploadAwards.setSubsidyAmountExact((currentCell.getStringCellValue()));
	        	  }else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
	        		 bulkUploadAwards.setSubsidyAmountExact((String.valueOf(currentCell.getNumericCellValue())));
	        	  }
	        	  //System.out.println(bulkUploadAwards);
	        	  break;
	           
	          case 8:
	        	  if(currentCell.getCellType()==CellType.BLANK) {
	        		  bulkUploadAwards.setNationalIdType(null);
	        	  }else {
	        		  bulkUploadAwards.setNationalIdType(currentCell.getStringCellValue());
	        	  }
	        	 
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 9:
	        	
	        	  bulkUploadAwards.setNationalId( ((currentCell.getCellType().getCode() == CellType.NUMERIC.getCode()) ?  String.valueOf(Double.valueOf( currentCell.getNumericCellValue()).longValue()) : currentCell.getStringCellValue()));
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 10:
	        	  if(currentCell.getCellType()==CellType.BLANK) {
	        		  bulkUploadAwards.setBeneficiaryName(null);
	        	  }else {
	        		  bulkUploadAwards.setBeneficiaryName(currentCell.getStringCellValue());
	        	  }
	        	  
	        	 // System.out.println(bulkUploadAwards);
	            break;

	          case 11:
	        	  if(currentCell.getCellType()==CellType.BLANK) {
	        		  bulkUploadAwards.setOrgSize(null);
	        	  }else {
	        	  bulkUploadAwards.setOrgSize(currentCell.getStringCellValue());
	        	  }
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 12:
	        	  if(currentCell.getCellType()==CellType.BLANK) {
	        		  bulkUploadAwards.setGrantingAuthorityName(null);
	        	  }else {
	        	  bulkUploadAwards.setGrantingAuthorityName(currentCell.getStringCellValue());
	        	  }
	        	  //System.out.println(bulkUploadAwards);
	            break;
	            
	          case 13:
	        	  
	        	  bulkUploadAwards.setLegalGrantingDate(convertDateToString(currentCell.getDateCellValue()));
	        	
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
	      throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	log.info("fail to read Excel file: " + e);
		      throw new RuntimeException("fail to read Excel file: " + e.getMessage());
		    }
	  }
	
	private static String convertDateToString(Date incomingDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		String date = null ;
		if(incomingDate!=null) {
		try {
			date = formatter.format(incomingDate).toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		}
		
		return date;
	}
	
	/*private static String convertDateToString(String incomingDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		String date = null ;
		if(incomingDate!=null) {
		try {
			date = formatter.format(incomingDate).toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		}
		
		return date;
	}*/
	
	public static boolean containsValue(Row row, int fcell, int lcell) 
	{
	    boolean flag = true;
	    if ((StringUtils.isEmpty(String.valueOf(row.getCell(0))) == true && 
	    		StringUtils.isEmpty(String.valueOf(row.getCell(1))) == true) ||
	    (String.valueOf(row.getCell(0))==null && String.valueOf(row.getCell(1))==null))
	    {
	    	return false;
	    }
	    /*for (int i = fcell; i < 16; i++) {
	    if (StringUtils.isEmpty(String.valueOf(row.getCell(i))) == true || 
	        StringUtils.isWhitespace(String.valueOf(row.getCell(i))) == true || 
	        StringUtils.isBlank(String.valueOf(row.getCell(i))) == true || 
	        String.valueOf(row.getCell(i)).length() == 0 || 
	        row.getCell(i) == null) {flag= false;} 
	    else {
	    			System.out.println("it is not empty row ");
	                flag = true;
	        }
	    }*/
	    return flag;    
	   
	}
	  

}
