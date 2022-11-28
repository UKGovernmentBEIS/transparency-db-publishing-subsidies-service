package com.beis.subsidy.award.transperancy.dbpublishingservice.repository;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAAward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MFAAwardRepositoryTest {

	public MFAAwardRepository mfaAwardRepository = mock(MFAAwardRepository.class);

	MFAAward mfaAward;

	@BeforeEach
	public void setUp() {
		mfaAward = new MFAAward();
		mfaAward.setMfaAwardNumber(2L);
	}

	@Test
	public void findByMfaAwardNumberTest() {
		when(mfaAwardRepository.findByMfaAwardNumber(anyLong())).thenReturn(mfaAward);

		MFAAward result = mfaAwardRepository.findByMfaAwardNumber(2);

		assertThat(result).isNotNull();
		assertThat(result.getMfaAwardNumber()).isEqualTo(2L);
	}
}
