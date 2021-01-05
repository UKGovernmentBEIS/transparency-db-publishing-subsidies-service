package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;

@FeignClient(name="process-micro-service", url="localhost:8081")
//@FeignClient(name="process-micro-service", url="process-micro-service.azurewebsites.net")
public interface ProcessServiceProxy {
	
	@PostMapping("/process-api/bulkAwards")
	public ResponseEntity<Object> createBulkTemplateAwards(@RequestBody List<BulkUploadAwards> bulkAwards);
	
	@GetMapping("/awards")
	public ResponseEntity<List<Award>> getAllAwards();
	
	@GetMapping("/subsidyMeasures")
	public ResponseEntity<List<SubsidyMeasure>> getAllSubsidyMeasures();
	
	@GetMapping("/grantingAuthorities")
	public ResponseEntity<List<GrantingAuthority>> getAllGrantingAuthorities();		
}
