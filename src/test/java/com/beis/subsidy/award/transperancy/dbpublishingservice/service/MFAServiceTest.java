package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.UserPrinciple;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAGrouping;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.MFAGroupingRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MFAGroupingRequest;
import static org.assertj.core.api.Assertions.assertThat;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.stereotype.Service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@Service
public class MFAServiceTest {
    @InjectMocks
    private MFAService mfaServiceMock;

    private final GrantingAuthorityRepository grantingAuthorityRepositoryMock = mock(GrantingAuthorityRepository.class);
    private final MFAGroupingRepository mfaGroupingRepositoryMock = mock(MFAGroupingRepository.class);
    UserPrinciple upMock;
    MFAGroupingRequest mfaGroupingRequest;
    GrantingAuthority grantingAuthority;

    @BeforeEach
    public void setUp(){
        upMock = mock(UserPrinciple.class);

        mfaGroupingRequest = new MFAGroupingRequest();
        mfaGroupingRequest.setMfaGroupingName("MFA Grouping Name");
        mfaGroupingRequest.setStatus("Active");
        mfaGroupingRequest.setGrantingAuthorityName("TEST GA");

        grantingAuthority = new GrantingAuthority();
        grantingAuthority.setGrantingAuthorityName("TEST GA");
        grantingAuthority.setStatus("Active");
        grantingAuthority.setGaId(26L);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddMfaGrouping(){
        MFAGrouping mfaGroupingReturn = new MFAGrouping();
        mfaGroupingReturn.setMfaGroupingNumber("MFA10001");

        when(grantingAuthorityRepositoryMock.findByGrantingAuthorityName(mfaGroupingRequest.getGrantingAuthorityName().trim())).thenReturn(grantingAuthority);
        when(upMock.getUserName()).thenReturn("SYSTEM");
        when(mfaGroupingRepositoryMock.save(any(MFAGrouping.class))).thenReturn(mfaGroupingReturn);

        String result = mfaServiceMock.addMfaGrouping(mfaGroupingRequest, upMock);

        assertThat(result).isEqualTo("MFA10001");
    }

    @Test
    public void testAddMfaGroupingNameMissing(){
        mfaGroupingRequest.setMfaGroupingName("");
        String result = mfaServiceMock.addMfaGrouping(mfaGroupingRequest, upMock);
        assertThat(result).isNull();
    }

}
