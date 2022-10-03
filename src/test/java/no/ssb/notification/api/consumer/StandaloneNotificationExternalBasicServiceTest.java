package no.ssb.notification.api.consumer;

import no.altinn.schemas.services.serviceengine.notification._2015._06.SendNotificationResultList;
import no.altinn.schemas.services.serviceengine.standalonenotificationbe._2009._10.StandaloneNotificationBEList;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasic;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage;
import no.altinn.services.serviceengine.notification._2010._10.SendStandaloneNotificationBasicV3Response;
import no.ssb.domene.altinn.correspondence.CommonNotification;
import no.ssb.domene.altinn.correspondence.LanguageCode;
import no.ssb.domene.altinn.correspondence.NotificationReportee;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StandaloneNotificationExternalBasicServiceTest {

    @InjectMocks
    StandaloneNotificationExternalBasicService standaloneNotificationExternalBasicService;
    @Mock
    INotificationAgencyExternalBasic iNotificationAgencyExternalBasic;

    String datoFormat = "yyyy-MM-dd HH:mm:ss";
    String datoString = "2019-11-26 08:28:40";
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(datoFormat);
    CommonNotification commonNotification = new CommonNotification();
    NotificationReportee notificationReportee = new NotificationReportee();

    @Before
    public void setUp() throws Exception {
//        XMLGregorianCalendar xmlGregCalSendDate = DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-11-26T08:28:40.317-04:00");
        Date dato = new SimpleDateFormat(datoFormat).parse(datoString);

        commonNotification.setFromAddress("no-reply@ssb.no");
        commonNotification.setNotificationType("SSB_DIG_Tilpasset_uten_revarsling_epost_sms");
        commonNotification.setSendDateTime(LocalDateTime.parse(datoString, dtf));
//        commonNotification.setSendDateTime(dato);
        commonNotification.setTransportType("Both");

        notificationReportee.setReportee("987654321");
        notificationReportee.setReceiverAddress("rsa@ssb.no");
        notificationReportee.setLanguageCode(LanguageCode.NORSK);
        notificationReportee.setServiceCode("1234");
        notificationReportee.setServiceEditionCode("2");
        notificationReportee.setNotificationSMS("bokmål sms");
        notificationReportee.setNotificationSubject("bokmål varsel");
        notificationReportee.setNotificationText("bokmål varseltekst");

    }

    @After
    public void tearDown() throws Exception {
        commonNotification = null;
        notificationReportee = null;
    }

    @Ignore
    @Test
    public void test_sendNotification() {
        try {
            when(iNotificationAgencyExternalBasic.sendStandaloneNotificationBasicV3(any(String.class), any(String.class), any(StandaloneNotificationBEList.class)))
                    .thenReturn(createSendNotificationResultListMock());
            SendNotificationResultList  sendNotificationResultList =
                    standaloneNotificationExternalBasicService.sendNotification(notificationReportee, commonNotification);
            assertEquals(1, sendNotificationResultList.getNotificationResult().size());
        } catch (INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage iNotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage) {
            iNotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage.printStackTrace();
        }
    }

    private SendNotificationResultList createSendNotificationResultListMock() {
        try {
            File responseFile = new File("src/test/resources/StandaloneNotification_response.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(SendStandaloneNotificationBasicV3Response.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SendStandaloneNotificationBasicV3Response response = (SendStandaloneNotificationBasicV3Response)jaxbUnmarshaller.unmarshal(responseFile);

            SendNotificationResultList resultList = response.getSendStandaloneNotificationBasicV3Result().getValue();
            return resultList;

        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }


    }
}