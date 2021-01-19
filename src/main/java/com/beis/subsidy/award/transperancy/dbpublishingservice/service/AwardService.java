package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.SubsidyMeasureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Beneficiary;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AwardRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.BeneficiaryRepository;

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
	
	private Date convertToDateSingleUpload(String incomingDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
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
		List<Beneficiary> beneficiaries = bulkAwards.stream()
			.map( award -> {
				Beneficiary beneficiary = new Beneficiary();
				beneficiary.setBeneficiaryName(award.getBeneficiaryName());
				beneficiary.setBeneficiaryType("Individual");
				beneficiary.setNationalId(award.getNationalId());
				beneficiary.setNationalIdType(award.getNationalIdType());
				beneficiary.setOrgSize(award.getOrgSize());
				beneficiary.setCreatedBy("SYSTEM");
				beneficiary.setApprovedBy("SYSTEM");
				beneficiary.setStatus("DRAFT");
				beneficiary.setSicCode("14455");
				beneficiary.setCreatedTimestamp(LocalDate.now());
				beneficiary.setLastModifiedTimestamp(LocalDate.now());
				return beneficiary;
				})
			.collect(Collectors.toList());
		
		beneficiaryRepository.saveAll(beneficiaries);

		List<Award> awards = bulkAwards.stream()
				.map( bulkaward -> new Award(null, getBeneficiaryDetails(bulkaward, beneficiaries), getGrantingAuthority(bulkaward), getSubsidyMeasure(bulkaward), bulkaward.getSubsidyAmountRange(), 
						( (bulkaward.getSubsidyAmountExact() != null) ? new BigDecimal(bulkaward.getSubsidyAmountExact()) : BigDecimal.ZERO),  
						((bulkaward.getSubsidyObjective().equalsIgnoreCase("Other"))? "Other - "+bulkaward.getSubsidyObjectiveOther():bulkaward.getSubsidyObjective()), bulkaward.getGoodsOrServices(),
						convertToDate(bulkaward.getLegalGrantingDate()),
						convertToDate(bulkaward.getLegalGrantingDate()),
						bulkaward.getSpendingRegion(), 
						((bulkaward.getSubsidyInstrument().equalsIgnoreCase("Other"))? "Other - "+bulkaward.getSubsidyInstrumentOther():bulkaward.getSubsidyInstrument()),
						bulkaward.getSpendingSector(),
						"SYSTEM", 
						"SYSTEM", 
						"Awaiting Approval",null,LocalDate.now(), LocalDate.now())
				
					)
				.collect(Collectors.toList());
				
		List<Award> savedAwards = awardRepository.saveAll(awards);
		log.info("End process Bulk Awards db");
				
		return savedAwards;
		} catch(Exception serviceException) {
			log.info("serviceException occured::"+serviceException.getMessage());
			return null;
		}
	}
	
	@Transactional
	public Award createAward(SingleAward award) {
		try {
			log.info("inside process Bulk Awards db");

			Beneficiary beneficiary = new Beneficiary();
			beneficiary.setBeneficiaryName(award.getBeneficiaryName());
			beneficiary.setBeneficiaryType("Individual");
			beneficiary.setNationalId(award.getNationalId());
			beneficiary.setNationalIdType(award.getNationalIdType());
			beneficiary.setOrgSize(award.getOrgSize());
			beneficiary.setCreatedBy("SYSTEM");
			beneficiary.setApprovedBy("SYSTEM");
			beneficiary.setStatus("DRAFT");
			beneficiary.setSicCode("14455");
			beneficiary.setCreatedTimestamp(LocalDate.now());
			beneficiary.setLastModifiedTimestamp(LocalDate.now());

			beneficiaryRepository.save(beneficiary);

			BulkUploadAwards tempAward = new BulkUploadAwards();
			tempAward.setGrantingAuthorityName(award.getGrantingAuthorityName());
			tempAward.setSubsidyControlTitle(award.getSubsidyControlTitle());

			Award saveAward = new Award(null, beneficiary, getGrantingAuthority(tempAward),
					getSubsidyMeasure(tempAward), award.getSubsidyAmountRange(),
					((award.getSubsidyAmountExact() != null) ? new BigDecimal(award.getSubsidyAmountExact())
							: BigDecimal.ZERO),
					((award.getSubsidyObjective().equalsIgnoreCase("Other")) ? "Other - "+award.getSubsidyObjectiveOther()
							: award.getSubsidyObjective()),
					award.getGoodsOrServices(), convertToDateSingleUpload(award.getLegalGrantingDate()),
					convertToDateSingleUpload(award.getLegalGrantingDate()), award.getSpendingRegion(),
					((award.getSubsidyInstrument().equalsIgnoreCase("Other")) ? "Other - "+award.getSubsidyInstrumentOther()
							: award.getSubsidyInstrument()),
					award.getSpendingSector(), "SYSTEM", "SYSTEM", "Awaiting Approval", null,LocalDate.now(), LocalDate.now());

			Award savedAwards = awardRepository.save(saveAward);
			log.info("End process Bulk Awards db");

			return savedAwards;
		} catch (Exception serviceException) {
			log.info("serviceException occured::" + serviceException.getMessage());
			return null;
		}
	}

	private Beneficiary getBeneficiaryDetails(BulkUploadAwards bulkAward, List<Beneficiary> beneficiaries) {
		
		Optional<Beneficiary> benfOptional = beneficiaries.stream().filter(bene -> bene.getBeneficiaryName().equals(bulkAward.getBeneficiaryName())).findAny();
		
		return ( (benfOptional != null) ? benfOptional.get() : null );
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
		
		log.info("Inside getSubsidyControlId..."+award.getSubsidyControlTitle());
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
