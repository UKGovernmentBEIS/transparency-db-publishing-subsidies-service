package com.beis.subsidy.award.transperancy.dbpublishingservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;

public class AwardRepositoryTest {

	public AwardRepository awardRepository = mock(AwardRepository.class);

	@Test
	public void findByServiceCodeTest() {
		when(awardRepository.save(new Award())).thenReturn(new Award());
		Award award = awardRepository.save(new Award());
		assertThat(award).isNotNull();
	}
}
