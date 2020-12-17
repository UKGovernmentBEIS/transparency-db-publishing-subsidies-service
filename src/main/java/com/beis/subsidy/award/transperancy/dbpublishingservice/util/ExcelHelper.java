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

public class ExcelHelper {
	
	public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	
	/*
	public  static String[] HEADERs = { 
			"Subsidy Measure Title",
			"Subsidy Control Number",
			"National ID Type",
			"National ID",
			"Name of the beneficiary",
			"Size of Organisation",
			"Subsidy instrument",
			"Subsidy Objective",
			"Subsidy Element Full amount Range",
			"Subsidy Element Full amount Exact (£)",
			"Legal Granting Date",
			"Granting Authority Name",
			"Goods or Services",
			"Spending Region",
	};*/
	
	
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
		System.out.println("hasExcelFormat true BulkUploadAwardsController");
		return true;
	}

	/*
	public static List<BulkUploadAwards> excelToAwards(InputStream is) {
	    try {
	       // List<Test> tempStudentList = new ArrayList<Test>();
	        //XSSFWorkbook workbook = new XSSFWorkbook(reapExcelDataFile.getInputStream());
	        //XSSFSheet worksheet = workbook.getSheetAt(0);
	        
	        Workbook workbook = new XSSFWorkbook(is);

	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      List<BulkUploadAwards> bulkUploadAwardsList = new ArrayList<BulkUploadAwards>();

	      int rowNumber = 0;
	      while (rows.hasNext()) {
	        Row currentRow = rows.next();

	        // skip header
	        if (rowNumber == 0) {
	          rowNumber++;
	          continue;
	        }

	        Iterator<Cell> cellsInRow = currentRow.iterator();

	        BulkUploadAwards bulkUploadAwards = new BulkUploadAwards();
	        bulkUploadAwards.setRow(currentRow.getRowNum() + 1);

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	          Cell currentCell = cellsInRow.next();

	          System.out.println("Going Inside switch block");
	          switch (cellIdx) {
	          
	          case 0:
	        	  bulkUploadAwards.setSubsidyControlTitle(currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;

	          case 1:
	        	  bulkUploadAwards.setSubsidyControlNumber(currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;

	          case 2:
	        	  bulkUploadAwards.setNationalIdType(currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;

	          case 3:
	        	  bulkUploadAwards.setNationalId(currentCell.getStringCellValue());
	        	  System.out.println("Naional id = " + currentCell.getStringCellValue());
	        	  break;
	            
	          case 4:
	        	  bulkUploadAwards.setBeneficiaryName(currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	        	  break;
	            
	          case 5:
	        	  bulkUploadAwards.setOrgSize(currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;
	            
	          case 6:
	        	  bulkUploadAwards.setSubsidyInstrument(currentCell.getStringCellValue());
	        	  System.out.println("Subsidy instrument = " + currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;
	            
	          case 7:
	        	  bulkUploadAwards.setSubsidyObjective(currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;
	            
	          case 8:
	        	  bulkUploadAwards.setSubsidyAmountRange(currentCell.getStringCellValue());
	        	  System.out.println("currentCell.getStringCellValue() = " + currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);

	            break;
	            
	          case 9:
	        	  bulkUploadAwards.setSubsidyAmountExact(currentCell.getStringCellValue());	        	  
	        	  System.out.println("currentCell.getStringCellValue() = " + currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;
	            
	          case 10:
	        	  bulkUploadAwards.setLegalGrantingDate(convertDateToString(currentCell.getDateCellValue()));
	        	  System.out.println("getNumericCellValue - setLegalGrantingDate = " + currentCell.getNumericCellValue());
	        	  System.out.println("getDateCellValue - setLegalGrantingDate = " + currentCell.getDateCellValue());

	        	  System.out.println(bulkUploadAwards);
	            break;
	            
	          case 11:
	        	  bulkUploadAwards.setGrantingAuthorityName(currentCell.getStringCellValue());
	        	  System.out.println("currentCell.getStringCellValue() = " + currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;
	            
	          case 12:
	        	  bulkUploadAwards.setGoodsOrServices(currentCell.getStringCellValue());
	        	  System.out.println("currentCell.getStringCellValue() = " + currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;
	            
	          case 13:
	        	  bulkUploadAwards.setSpendingRegion(currentCell.getStringCellValue());
	        	  System.out.println("currentCell.getStringCellValue() = " + currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;
	            
	          default:
	            break;
	          }

	          cellIdx++;
	        }

	        System.out.println(bulkUploadAwards);
	        bulkUploadAwardsList.add(bulkUploadAwards);
	      }

	      workbook.close();

	      System.out.println("Excel - List - size = " + bulkUploadAwardsList.size());
	      return bulkUploadAwardsList;
	    } catch (IOException e) {
	      throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
	    }
	  }*/
	
	public static List<BulkUploadAwards> excelToAwards(InputStream is) {
	    try {
	       // List<Test> tempStudentList = new ArrayList<Test>();
	        //XSSFWorkbook workbook = new XSSFWorkbook(reapExcelDataFile.getInputStream());
	        //XSSFSheet worksheet = workbook.getSheetAt(0);
	    	
	    	System.out.println("ïnside excelToAwards::DBPublishingSubsideies Service" );
	        
	        Workbook workbook = new XSSFWorkbook(is);

	      Sheet sheet = workbook.getSheet(SHEET);
	      Iterator<Row> rows = sheet.iterator();

	      System.out.println("first row "+sheet.getFirstRowNum());
	      List<BulkUploadAwards> bulkUploadAwardsList = new ArrayList<BulkUploadAwards>();
	      System.out.println("last row "+sheet.getLastRowNum());
	      int rowNumber = 0;
	      while (rows.hasNext()) {
	        Row currentRow = rows.next();

	        System.out.println("BulkUploadAwardsController Going Inside switch block");
	        // skip header
	        if (rowNumber == 0) {
	          rowNumber++;
	          continue;
	        }
	        int fcell = currentRow.getFirstCellNum();// first cell number of excel
	        int lcell = currentRow.getLastCellNum();
	        if(containsValue(currentRow,fcell,lcell)) {
	        	System.out.println("am inside contains");
	        	
	        
	        Iterator<Cell> cellsInRow = currentRow.iterator();

	        BulkUploadAwards bulkUploadAwards = new BulkUploadAwards();
	        bulkUploadAwards.setRow(currentRow.getRowNum() + 1);

	        int cellIdx = 0;
	        while (cellsInRow.hasNext()) {
	          Cell currentCell = cellsInRow.next();
	          
	          switch (cellIdx) {
	          
	          case 0:
	        	  bulkUploadAwards.setSubsidyControlNumber(currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;

	          case 1:
	        	  bulkUploadAwards.setSubsidyControlTitle(currentCell.getStringCellValue());
	        	  //System.out.println(bulkUploadAwards);
	            break;

	          case 2:
	        	 
	        	  bulkUploadAwards.setSubsidyObjective(currentCell.getStringCellValue());
	        	  System.out.println(bulkUploadAwards);
	            break;
	            
	          case 3:
		        	 
	        	  bulkUploadAwards.setSubsidyObjectiveOther(currentCell.getStringCellValue());
	        	  //System.out.println(bulkUploadAwards);
	            break;

	          case 4:
	        	  
	        	  bulkUploadAwards.setSubsidyInstrument(currentCell.getStringCellValue());
	        	  break;
	            
	          case 5:
	        	  	
	        	  bulkUploadAwards.setSubsidyInstrumentOther(currentCell.getStringCellValue());	
	        	  System.out.println(bulkUploadAwards);
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
	        	 
	        	  bulkUploadAwards.setNationalIdType(currentCell.getStringCellValue());
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 9:
	        	
	        	  bulkUploadAwards.setNationalId( ((currentCell.getCellType().getCode() == CellType.NUMERIC.getCode()) ?  String.valueOf(Double.valueOf( currentCell.getNumericCellValue()).longValue()) : currentCell.getStringCellValue()));
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 10:
	        	  
	        	  bulkUploadAwards.setBeneficiaryName(currentCell.getStringCellValue());
	        	 // System.out.println(bulkUploadAwards);
	            break;

	          case 11:
	        	 
	        	  bulkUploadAwards.setOrgSize(currentCell.getStringCellValue());
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 12:
	        	 
	        	  bulkUploadAwards.setGrantingAuthorityName(currentCell.getStringCellValue());
	        	  //System.out.println(bulkUploadAwards);
	            break;
	            
	          case 13:
	        	  //bulkUploadAwards.setGrantingAuthorityName(currentCell.getStringCellValue());
	        	  //System.out.println("currentCell.getStringCellValue() = " + currentCell.getStringCellValue());
	        	  bulkUploadAwards.setLegalGrantingDate(convertDateToString(currentCell.getDateCellValue()));
	        	 // System.out.println("getNumericCellValue - setLegalGrantingDate = " + currentCell.getNumericCellValue());
	        	 // System.out.println("getDateCellValue - setLegalGrantingDate = " + currentCell.getDateCellValue());
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 14:
	        	  bulkUploadAwards.setGoodsOrServices(currentCell.getStringCellValue());
	        	  //System.out.println("currentCell.getStringCellValue() = " + currentCell.getStringCellValue());
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 15:
	        	  bulkUploadAwards.setSpendingRegion(currentCell.getStringCellValue());
	        	 // System.out.println("currentCell.getStringCellValue() = " + currentCell.getStringCellValue());
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          case 16:
	        	  bulkUploadAwards.setSpendingSector(currentCell.getStringCellValue());
	        	  //System.out.println("currentCell.getStringCellValue() = " + currentCell.getStringCellValue());
	        	 // System.out.println(bulkUploadAwards);
	            break;
	            
	          default:
	            break;
	          }

	          cellIdx++;
	        }

	        //System.out.println("Row overrrrr "+bulkUploadAwards);
	        bulkUploadAwardsList.add(bulkUploadAwards);
	        }else {
	        	break;
	        }
	      }

	      workbook.close();

	      System.out.println("Excel - List - size = " + bulkUploadAwardsList.size());
	      return bulkUploadAwardsList;
	    } catch (IOException e) {
	    	System.out.println("fail to parse Excel file: " + e.getMessage());
	      throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	System.out.println("fail to read Excel file: " + e);
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
