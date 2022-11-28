package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.MFAController;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.AuditLogs;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.MFAAwardRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MFAAwardRequest;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.MFAService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MFAControllerTest {
    @InjectMocks
    private MFAController mfaController;

    // Mocks
    @Mock
    ObjectMapper omMock;
    @Mock
    MFAService mfaServiceMock;
    @Mock
    MFAAwardRepository mfaAwardRepositoryMock;
    @Mock
    AuditLogsRepository auditLogsRepositoryMock;

    // Actuals
    MFAAwardRequest mfaAwardRequest;
    UserPrinciple userPrinciple;
    HttpHeaders httpHeaders;
    MFAAwardResponse mfaAwardResponse;
    MFAAward mfaAward;
    GrantingAuthority grantingAuthority;


    @BeforeEach
    public void setUp(){
        userPrinciple = new UserPrinciple();
        mfaAwardRequest = new MFAAwardRequest();
        httpHeaders = new HttpHeaders();
        mfaAwardResponse = new MFAAwardResponse();
        grantingAuthority = new GrantingAuthority();
        mfaAward = new MFAAward();

        httpHeaders.add("userprinciple", "{\"userName\":\"BEIS Admin\",\"password\":\"password123\",\"role\":\"BEIS Administrator\",\"grantingAuthorityGroupId\":\"2\",\"grantingAuthorityGroupName\":\"TEST GA\"}");

        userPrinciple.setUserName("BEIS Admin");
        userPrinciple.setGrantingAuthorityGroupId(2);
        userPrinciple.setGrantingAuthorityGroupName("TEST GA");
        userPrinciple.setRole("BEIS Administrator");

        mfaAwardRequest.setSpeiAssistance(false);
        mfaAwardRequest.setMfaGroupingPresent(false);
        mfaAwardRequest.setAwardFullAmount(new BigDecimal(10000));
        mfaAwardRequest.setConfirmationDate("2022-01-31");
        mfaAwardRequest.setGrantingAuthorityName("TEST GA");
        mfaAwardRequest.setBeneficiaryName("This Charity");
        mfaAwardRequest.setNationalIdType("Charity Number");
        mfaAwardRequest.setNationalIdNumber("12345678");
        mfaAwardRequest.setStatus("Published");

        mfaAwardResponse.setMfaAwardNumber(1L);

        grantingAuthority.setGrantingAuthorityName("TEST GA");
        grantingAuthority.setStatus("Active");
        grantingAuthority.setGaId(1L);
        grantingAuthority.setAzureGroupId("group-id");

        mfaAward.setGrantingAuthority(grantingAuthority);
        mfaAward.setStatus("Published");

        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddMfaAward() throws JsonProcessingException {
        Long expected = 5L;

        when(omMock.readValue(anyString(), eq(UserPrinciple.class))).thenReturn(userPrinciple);
        when(mfaServiceMock.addMfaAward(mfaAwardRequest, userPrinciple)).thenReturn(expected);

        Long result = mfaController.addMfaAward(httpHeaders, mfaAwardRequest);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testFindMfaAward() throws JsonProcessingException {
        when(omMock.readValue(anyString(), eq(UserPrinciple.class))).thenReturn(userPrinciple);
        when(mfaServiceMock.findMfaAwardById(anyLong())).thenReturn(mfaAwardResponse);
        when(mfaAwardRepositoryMock.findByMfaAwardNumber(mfaAwardResponse.getMfaAwardNumber())).thenReturn(mfaAward);

        ResponseEntity<MFAAwardResponse> result = mfaController.findMfaAward(httpHeaders,"1");

        MFAAwardResponse resultResponse = result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert resultResponse != null;
        assertThat(resultResponse.getMfaAwardNumber()).isEqualTo(1L);
    }

    @Test
    public void testUpdateMfaAward() throws JsonProcessingException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(omMock.readValue(anyString(), eq(UserPrinciple.class))).thenReturn(userPrinciple);
        when(mfaServiceMock.updateMfaAward(mfaAwardRequest, 1L, userPrinciple)).thenReturn(1L);
        when(mfaAwardRepositoryMock.findByMfaAwardNumber(mfaAwardResponse.getMfaAwardNumber())).thenReturn(mfaAward);
        when(auditLogsRepositoryMock.save(any(AuditLogs.class))).thenReturn(null);

        String result = mfaController.updateMfaAwardDetails(httpHeaders,mfaAwardRequest, "1", response);

        assertThat(result != null);
        assertThat(result).isEqualTo("1");
    }

    @Test
    public void testDeleteMfaAward() throws JsonProcessingException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        mfaAwardRequest.setStatus("Deleted");

        when(omMock.readValue(anyString(), eq(UserPrinciple.class))).thenReturn(userPrinciple);
        when(mfaServiceMock.updateMfaAward(mfaAwardRequest, 1L, userPrinciple)).thenReturn(1L);
        when(mfaAwardRepositoryMock.findByMfaAwardNumber(mfaAwardResponse.getMfaAwardNumber())).thenReturn(mfaAward);
        when(auditLogsRepositoryMock.save(any(AuditLogs.class))).thenReturn(null);

        String result = mfaController.updateMfaAwardDetails(httpHeaders,mfaAwardRequest, "1", response);

        assertThat(result != null);
        assertThat(result).isEqualTo("1");
    }
}
