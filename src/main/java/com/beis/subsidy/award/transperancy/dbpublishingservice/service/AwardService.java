package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.SubsidyMeasureRepository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${loggingComponentName}")
	private String loggingComponentName;
	
	private Date convertToDate(String incomingDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		Date date = null ;
		try {
			date = formatter.parse(incomingDate);
		} catch (ParseException e) {
		  log.error("{}:: date error parse issue{}",loggingComponentName,e);
		}
		
		return date;
	}
	
	private Date convertToDateSingleUpload(String incomingDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
		Date date = null ;
		try {
			date = formatter.parse(incomingDate);
		} catch (ParseException e) {
			log.error("{}:: date error parse issue{}",loggingComponentName,e);
		}
		
		return date;
	}

	@Transactional
	public List<Award> saveAwards(List<Award> awards) {
		
		List<Beneficiary> beneficiaries = beneficiaryRepository.saveAll(
					awards.stream()
					.map(award -> award.getBeneficiary()).collect(Collectors.toList()));
		
		return awardRepository.saveAll(awards);
	}
	
	@Transactional
	public List<Award> processBulkAwards(List<BulkUploadAwards> bulkAwards, String role) {
		try {
		log.info("{} ::inside process Bulk Awards db",loggingComponentName);
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
						addAwardStatus(role),null,LocalDate.now(), LocalDate.now())
				
					)
				.collect(Collectors.toList());
				
		List<Award> savedAwards = awardRepository.saveAll(awards);
		log.info("End process Bulk Awards db");
				
		return savedAwards;
		} catch(Exception serviceException) {
			log.error("serviceException occured::" , serviceException);
			return null;
		}
	}

	private String addAwardStatus(String role) {
		String awardStatus = "Published";
		if ("Granting Authority Encoder".equals(role.trim())) {
			awardStatus = "Awaiting Approval";
		}
		return awardStatus;
	}

	@Transactional
	public Award createAward(SingleAward award, String role) {
		try {
			log.info("inside process Bulk Awards db");
			String awardStatus = "Published";
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
			tempAward.setSubsidyControlNumber(award.getSubsidyControlNumber());

			if ("Granting Authority Encoder".equals(role.trim())) {
				awardStatus = "Awaiting Approval";
			}

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
					award.getSpendingSector(), "SYSTEM", "SYSTEM", awardStatus, null,LocalDate.now(), LocalDate.now());

			Award savedAwards = awardRepository.save(saveAward);
			log.info("{} :: End process Bulk Awards db");

			return savedAwards;
		} catch (Exception ex) {
			log.error("{} :: Award is failed to save ::{} and exception {}",ex.getMessage(),ex);
			return null;
		}
	}
	
	@Transactional
	public Award updateAward(Long awardNumber,SingleAward awardUpdateRequest, String role) {

		try {
			log.info("inside updateAward db");
			Award award = awardRepository.findByAwardNumber(awardNumber);
			if (Objects.isNull(award)) {

				throw new Exception("Award details not found::" + awardNumber);
			}

			award.setStatus(awardUpdateRequest.getStatus());

			award.setLastModifiedTimestamp(LocalDate.now());
			if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyAmountExact())) {
				award.setSubsidyFullAmountExact(new BigDecimal(awardUpdateRequest.getSubsidyAmountExact()));
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyAmountRange())) {
				award.setSubsidyFullAmountRange(awardUpdateRequest.getSubsidyAmountRange());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getSpendingRegion())) {
				award.setSpendingRegion(awardUpdateRequest.getSpendingRegion());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getSpendingSector())) {
				award.setSpendingSector(awardUpdateRequest.getSpendingSector());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getGoodsOrServices())) {
				award.setGoodsServicesFilter(awardUpdateRequest.getGoodsOrServices());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyObjective())) {
				award.setSubsidyObjective(awardUpdateRequest.getSubsidyObjective());
			} else if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyObjectiveOther())) {
				award.setSubsidyObjective(awardUpdateRequest.getSubsidyObjectiveOther());
			}

			if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyInstrument())) {
				award.setSubsidyInstrument(awardUpdateRequest.getSubsidyInstrument().trim());
			} else if(!StringUtils.isEmpty(awardUpdateRequest.getSubsidyInstrumentOther())){
				award.setSubsidyInstrument(awardUpdateRequest.getSubsidyInstrumentOther().trim());
			}

			if (!StringUtils.isEmpty(awardUpdateRequest.getLegalGrantingDate())) {
				award.setLegalGrantingDate(convertToDateSingleUpload(awardUpdateRequest.getLegalGrantingDate().trim()));
			}


			SubsidyMeasure measure = award.getSubsidyMeasure();

			if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyControlTitle())) {
				measure.setSubsidyMeasureTitle(awardUpdateRequest.getSubsidyControlTitle().trim());
			}
			award.setSubsidyMeasure(measure);
			Beneficiary beneficiaryDtls = award.getBeneficiary();
			if (!StringUtils.isEmpty(awardUpdateRequest.getNationalId())) {
				beneficiaryDtls.setNationalId(awardUpdateRequest.getNationalId().trim());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getNationalIdType())) {
				beneficiaryDtls.setNationalIdType(awardUpdateRequest.getNationalIdType().trim());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getBeneficiaryName())) {
				beneficiaryDtls.setBeneficiaryName(awardUpdateRequest.getBeneficiaryName().trim());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getOrgSize())) {
				beneficiaryDtls.setOrgSize(awardUpdateRequest.getOrgSize().trim());
			}
			award.setBeneficiary(beneficiaryDtls);

			GrantingAuthority grantingAuthority = award.getGrantingAuthority();
			if (!StringUtils.isEmpty(awardUpdateRequest.getGrantingAuthorityName())) {
				grantingAuthority.setGrantingAuthorityName(awardUpdateRequest.getGrantingAuthorityName().trim());
				award.setGrantingAuthority(grantingAuthority);
			}

			Award savedAwards = awardRepository.save(award);
			log.info("{} ::End of update Award info in db");
			return savedAwards;
		} catch (Exception serviceException) {
			log.error("serviceException occurred::", serviceException);
			return null;
		}
	}

	private Beneficiary getBeneficiaryDetails(BulkUploadAwards bulkAward, List<Beneficiary> beneficiaries) {
		
		Optional<Beneficiary> benfOptional = beneficiaries.stream().filter(bene -> bene.getBeneficiaryName().equals(bulkAward.getBeneficiaryName())).findAny();
		
		return ( (benfOptional != null) ? benfOptional.get() : null );
	}


	
	private SubsidyMeasure getSubsidyMeasure(BulkUploadAwards award) {

		log.info("Inside getSubsidyControlId..." + award.getSubsidyControlTitle());
		List<SubsidyMeasure> smList = smRepository.findAll();
		Optional<SubsidyMeasure> smOptional = null;
		if (!StringUtils.isEmpty(award.getSubsidyControlTitle())) {

			smOptional = smList.stream()
					.filter(sm -> sm.getSubsidyMeasureTitle().equals(award.getSubsidyControlTitle())).findAny();
		} else {
			log.info("inside else title");
			smOptional = smList.stream().filter(sm -> sm.getScNumber().equals(award.getSubsidyControlNumber()))
					.findAny();
		}

		return ((smOptional != null) ? smOptional.get() : null);
	}

	
	
	private GrantingAuthority getGrantingAuthority(BulkUploadAwards award) {
		
		log.info("Inside getGrantingAuthority...");

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
