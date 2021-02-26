package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@Slf4j
public class EmailUtils {

	@Autowired
	static Environment environment;

	public static void sendEmail(String emailId) throws NotificationClientException {
		NotificationClient client = new NotificationClient(environment.getProperty("apiKey"));
		SendEmailResponse response = client.sendEmail(environment.getProperty("templateId"), emailId, null, null);

		log.info("response :: " + response.getBody());
	}

	public static void sendSingleAwardEmail(String emailId, String encoder,String awardNumber) throws NotificationClientException {

		Map<String, Object> personalisation = new HashMap<>();
		personalisation.put("encoder_name", encoder);
		personalisation.put("award_number", awardNumber);

		/*NotificationClient client = new NotificationClient(environment.getProperty("apiKey"));
		SendEmailResponse response = client.sendEmail(environment.getProperty("new-user-mail-template"), emailId,
				personalisation, null);*/
		
		NotificationClient client = new NotificationClient("beis_notification-acabb994-cf6a-4d65-8632-1cc3ece74aa5-ef624de5-91dd-4f1b-8279-d970ee3949d5");
		SendEmailResponse response = client.sendEmail("65cdef6c-77b5-4a0b-b357-93ea6a7d121f", emailId,
				personalisation, null);

		log.info("response :: " + response.getBody());
	}
	
	public static void sendBuldAwardsEmail(String emailId, String encoder,String awardsCount) throws NotificationClientException {

		Map<String, Object> personalisation = new HashMap<>();
		personalisation.put("encoder_name", encoder);
		personalisation.put("award_count", awardsCount);

		/*NotificationClient client = new NotificationClient(environment.getProperty("apiKey"));
		SendEmailResponse response = client.sendEmail(environment.getProperty("new-user-mail-template"), emailId,
				personalisation, null);*/
		
		NotificationClient client = new NotificationClient("beis_notification-acabb994-cf6a-4d65-8632-1cc3ece74aa5-ef624de5-91dd-4f1b-8279-d970ee3949d5");
		SendEmailResponse response = client.sendEmail("175225eb-f873-4292-b775-dfc163defba6", emailId,
				personalisation, null);

		log.info("response :: " + response.getBody());
	}

	
	
}
