package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Beneficiary;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AwardRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.BeneficiaryRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.SubsidyMeasureRepository;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class AwardService {

	private static final String CONSTANT_SYSTEM = "SYSTEM";
	
	@Autowired
	private AwardRepository awardRepository;
	
	@Autowired
	private BeneficiaryRepository beneficiaryRepository;
	
	@Autowired
	private GrantingAuthorityRepository gaRepository;
	
	@Autowired
	private SubsidyMeasureRepository smRepository;
	
	public List<Award> getallAwards() {
		return awardRepository.findAll();
	}

	public Optional<Award> getAward(int id) {
		return awardRepository.findById(Long.valueOf(id));
	}

	/*
	public Award createAward(int subsidyControlId, String subdidyControlNumber, String subsidyInstrument,
			int gratingAuthorityId, int beneficiaryId, float subsidyFullAmount, float subsidyNotionalAmount,
			String susidyObjective, String gaSubsidyWeblink, String goodsServicesFilter, String legalBasis,
			String grantingLegalDate, String publishedAwardDate) {
		
		
		
		Award award = new Award();
		award.setAwardNumber(Long.valueOf(1111111));
		award.setSubdidyControlNumber(subdidyControlNumber);
		award.setSubsidyInstrument(subsidyInstrument);
		award.setGratingAuthorityId(Long.valueOf(gratingAuthorityId));
		award.setSubsidyFullAmountRange("10000 - 20000");
		award.setSubsidyFullAmountExact(BigDecimal.valueOf(subsidyNotionalAmount));
		award.setSusidyObjective(susidyObjective);
		award.setGoodsServicesFilter(goodsServicesFilter);
		award.setLegalGrantingDate(convertToDate(grantingLegalDate));
		award.setPublishedAwardDate(convertToDate(publishedAwardDate));		
		award.setCreatedBy(CONSTANT_SYSTEM);
		award.setApprovedBy(CONSTANT_SYSTEM);
		award.setStatus("DRAFT");
		return awardRepository.save(award);
		
	}*/

	private Date convertToDate(String incomingDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		Date date = null ;
		try {
			date = formatter.parse(incomingDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}

	@Transactional
	public Award save(Award award) {
		Beneficiary beneficiaries = beneficiaryRepository.save(award.getBeneficiary());
		return awardRepository.save(award);
	}
	
	@Transactional
	public List<Award> saveAwards(List<Award> awards) {
		
		List<Beneficiary> beneficiaries = beneficiaryRepository.saveAll(
					awards.stream()
					.map(award -> award.getBeneficiary()).collect(Collectors.toList()));
		
		return awardRepository.saveAll(awards);
	}
	
	@Transactional
	public List<Award> processBulkAwards(List<BulkUploadAwards> bulkAwards) {
		try {
		log.info("inside process Bulk Awards db");
		List<SubsidyMeasure> smList = getAllSubsidyMeasures();
		Map<String, String> smMap= smList.stream().collect(Collectors.toMap(SubsidyMeasure::getSubsidyMeasureTitle,SubsidyMeasure::getScNumber));
		System.out.println("smMap "+smMap);
		//log.info(smMap);
		
		List<Beneficiary> beneficiaries = bulkAwards.stream()
			.map( award -> new Beneficiary(	
								null, 
								null, 
								award.getBeneficiaryName(), 
								"Individual", 
								award.getNationalId(), 
								award.getNationalIdType(), 
								"14455", 
								award.getOrgSize(), 
								"E14 5AQ", 
								"Midlands", 
								"England", 
								"SYSTEM", 
								"SYSTEM", 
								"DRAFT", 
								null, 
								null
					)
				)
			.collect(Collectors.toList());
		
		beneficiaryRepository.saveAll(beneficiaries);
		//
		
		
		//
			
		
		List<Award> awards = bulkAwards.stream()
				.map( bulkaward -> new Award(null, getBeneficiaryDetails(bulkaward, beneficiaries), getGrantingAuthority(bulkaward), getSubsidyMeasure(bulkaward), bulkaward.getSubsidyAmountRange(), 
						( (bulkaward.getSubsidyAmountExact() != null) ? new BigDecimal(bulkaward.getSubsidyAmountExact()) : BigDecimal.ZERO),  
						((bulkaward.getSubsidyObjective().equalsIgnoreCase("Other"))? bulkaward.getSubsidyObjectiveOther():bulkaward.getSubsidyObjective()), bulkaward.getGoodsOrServices(),
						convertToDate(bulkaward.getLegalGrantingDate()),
						convertToDate(bulkaward.getLegalGrantingDate()),
						bulkaward.getSpendingRegion(), 
						((bulkaward.getSubsidyInstrument().equalsIgnoreCase("Other"))? bulkaward.getSubsidyInstrumentOther():bulkaward.getSubsidyInstrument()),
						bulkaward.getSpendingSector(),
						"SYSTEM", 
						"SYSTEM", 
						"DRAFT",null, null)
				
					)
				.collect(Collectors.toList());
				
		
		List<Award> savedAwards = awardRepository.saveAll(awards);
		log.info("End process Bulk Awards db");
				
		return savedAwards;
		}catch(Exception serviceException) {
			log.info("serviceException occured::"+serviceException.getMessage());
			return null;
		}
	}

	private Beneficiary getBeneficiaryDetails(BulkUploadAwards bulkaward, List<Beneficiary> beneficiaries) {
		
		Optional<Beneficiary> beneOptional = beneficiaries.stream().filter(bene -> bene.getBeneficiaryName().equals(bulkaward.getBeneficiaryName())).findAny();
		
		return ( (beneOptional != null) ? beneOptional.get() : null );
	}

private Long getBeneficiaryId(BulkUploadAwards bulkaward, List<Beneficiary> beneficiaries) {
		
		Optional<Beneficiary> beneOptional = beneficiaries.stream().filter(bene -> bene.getBeneficiaryName().equals(bulkaward.getBeneficiaryName())).findAny();
		
		return ( (beneOptional != null) ? beneOptional.get().getBeneficiaryId() : null );
	}

	private String getSubsidyControlId(BulkUploadAwards award) {
		
		log.info("Inside getSubsidyControlId...");
		List<SubsidyMeasure> smList = smRepository.findAll();
		
		Optional<SubsidyMeasure> smOptional = smList.stream().filter( sm -> sm.getSubsidyMeasureTitle().equals(award.getSubsidyControlTitle())).findAny();
		
		return ( (smOptional != null) ? smOptional.get().getScNumber(): null);
	}
	
private SubsidyMeasure getSubsidyMeasure(BulkUploadAwards award) {
		
		log.info("Inside getSubsidyControlId...");
		List<SubsidyMeasure> smList = smRepository.findAll();
		
		Optional<SubsidyMeasure> smOptional = smList.stream().filter( sm -> sm.getSubsidyMeasureTitle().equals(award.getSubsidyControlTitle())).findAny();
		
		return ( (smOptional != null) ? smOptional.get(): null);
	}

	private Long getGrantingAuthorityId(BulkUploadAwards award) {
		
		log.info("Inside getGrantingAuthorityId...");

		List<GrantingAuthority> gaList = gaRepository.findAll();
		
		log.info("All granting authority = " + gaList);
		
		Optional<GrantingAuthority> gaOptional = gaList.stream().filter(ga -> ga.getGrantingAuthorityName().equals(award.getGrantingAuthorityName())).findAny();
		
		log.info("Returning from getGrantingAuthorityId.. = " + gaOptional.get().getGaId());
		return ((gaOptional != null) ? gaOptional.get().getGaId() : null);
	}
	
	private GrantingAuthority getGrantingAuthority(BulkUploadAwards award) {
		
		log.info("Inside getGrantingAuthorityId...");

		List<GrantingAuthority> gaList = gaRepository.findAll();
		
		log.info("All granting authority = " + gaList);
		
		Optional<GrantingAuthority> gaOptional = gaList.stream().filter(ga -> ga.getGrantingAuthorityName().equals(award.getGrantingAuthorityName())).findAny();
		
		log.info("Returning from getGrantingAuthorityId.. = " + gaOptional.get().getGaId());
		return ((gaOptional != null) ? gaOptional.get() : null);
	}

	public List<SubsidyMeasure> getAllSubsidyMeasures() {
		return smRepository.findAll();
	}
	
	public List<GrantingAuthority> getAllGrantingAuthorities() {
		return gaRepository.findAll();
	}

	
	
	
}
