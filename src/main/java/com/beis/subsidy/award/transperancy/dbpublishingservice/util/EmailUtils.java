package com.beis.subsidy.award.transperancy.dbpublishingservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EmailUtils {

    public static void sendSingleAwardEmail(String emailId, String encoder, Long awardNumber, Environment environment)
    throws NotificationClientException {

        Map<String, Object> personalisation = new HashMap<>();
        personalisation.put("encoder_name", encoder);
        personalisation.put("award_number", awardNumber);

        if (!StringUtils.isEmpty(environment.getProperty("apiKey"))
            && !StringUtils.isEmpty(environment.getProperty("single-award-notification-template"))) {

            NotificationClient client = new NotificationClient(environment.getProperty("apiKey"));
            SendEmailResponse response = client.sendEmail(environment.getProperty("single-award-notification-template"), emailId,
                    personalisation, null);
            log.info(" single award email notification sent :: ");
        }
    }
}
