package no.ssb.notification.api.consumer;

import no.altinn.schemas.services.serviceengine.notification._2009._10.*;
import no.altinn.schemas.services.serviceengine.standalonenotificationbe._2015._06.Service;
import no.altinn.services.serviceengine.notification._2010._10.SendStandaloneNotificationBasicV3;
import no.ssb.domene.altinn.correspondence.CommonNotification;
import no.ssb.domene.altinn.correspondence.LanguageCode;
import no.ssb.domene.altinn.correspondence.NotificationReportee;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

import static org.junit.Assert.*;

public class StandaloneNotificationBuilderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    String datoFormat = "yyyy-MM-dd HH:mm:ss";
    String datoString = "2019-11-26 08:28:40";
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(datoFormat);

    CommonNotification commonNotification = new CommonNotification();
    NotificationReportee notificationReportee = new NotificationReportee();

    @Before
    public void setUp() throws Exception {

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

    @Test
    public void test_buildStandaloneNotification_norsk_both() {
        SendStandaloneNotificationBasicV3 sendStandaloneNotificationV3 =
                StandaloneNotificationBuilder.buildStandaloneNotification(notificationReportee, commonNotification);
        logger.info(sendStandaloneNotificationV3.toString());
        assertEquals(1,sendStandaloneNotificationV3.getStandaloneNotifications().getStandaloneNotification().size());
        StandaloneNotification s = sendStandaloneNotificationV3.getStandaloneNotifications().getStandaloneNotification().get(0);
        logger.info(s.toString());
        assertEquals("987654321", s.getReporteeNumber().getValue());
        assertEquals("no-reply@ssb.no", s.getFromAddress().getValue());
        assertEquals("SSB_DIG_Tilpasset_uten_revarsling_epost_sms", s.getNotificationType().getValue());
        assertEquals(1044, s.getLanguageID().intValue());

        assertEquals(2019, s.getShipmentDateTime().getYear());
        assertEquals(11, s.getShipmentDateTime().getMonth());
        assertEquals(26, s.getShipmentDateTime().getDay());
        assertEquals(8, s.getShipmentDateTime().getHour());
        assertEquals(28, s.getShipmentDateTime().getMinute());

        JAXBElement<Service> srv = s.getService();
        Service service = srv.getValue();
        assertEquals("1234", service.getServiceCode());
        assertEquals(2, service.getServiceEdition());

        JAXBElement<TextTokenSubstitutionBEList> t = s.getTextTokens();
        TextTokenSubstitutionBEList tList = t.getValue();
        assertEquals("bokmål sms", tList.getTextToken().get(0).getTokenValue().getValue());
        assertEquals("bokmål varsel", tList.getTextToken().get(1).getTokenValue().getValue());
        assertEquals("bokmål varseltekst", tList.getTextToken().get(2).getTokenValue().getValue());

        JAXBElement<ReceiverEndPointBEList> jaxbReceivers = s.getReceiverEndPoints();
        ReceiverEndPointBEList receivers = jaxbReceivers.getValue();
        List<ReceiverEndPoint> rList = receivers.getReceiverEndPoint();
        assertEquals(1, rList.size());
        assertEquals("Both", rList.get(0).getTransportType().getValue().value());
        assertEquals(null, rList.get(0).getReceiverAddress().getValue());
    }


    @Test
    public void test_buildStandaloneNotification_norsk_email() {
        commonNotification.setTransportType("Email");
        SendStandaloneNotificationBasicV3 sendStandaloneNotificationV3 =
                StandaloneNotificationBuilder.buildStandaloneNotification(notificationReportee, commonNotification);
        StandaloneNotification s = sendStandaloneNotificationV3.getStandaloneNotifications().getStandaloneNotification().get(0);

        ReceiverEndPoint receiver = s.getReceiverEndPoints().getValue().getReceiverEndPoint().get(0);
        assertEquals("Email", receiver.getTransportType().getValue().value());
        assertEquals("rsa@ssb.no", receiver.getReceiverAddress().getValue());
    }

    @Test
    public void test_buildStandaloneNotification_norsk_sms() {
        commonNotification.setTransportType("SMS");
        notificationReportee.setReceiverAddress("97719925");
        SendStandaloneNotificationBasicV3 sendStandaloneNotificationV3 =
                StandaloneNotificationBuilder.buildStandaloneNotification(notificationReportee, commonNotification);
        StandaloneNotification s = sendStandaloneNotificationV3.getStandaloneNotifications().getStandaloneNotification().get(0);

        ReceiverEndPoint receiver = s.getReceiverEndPoints().getValue().getReceiverEndPoint().get(0);
        assertEquals("SMS", receiver.getTransportType().getValue().value());
        assertEquals("97719925", receiver.getReceiverAddress().getValue());
    }

    @Test
    public void test_buildStandaloneNotification_norsk_emailPreferred() {
        commonNotification.setTransportType("EmailPreferred");
        notificationReportee.setReceiverAddress("rsa@ssb.no");
        SendStandaloneNotificationBasicV3 sendStandaloneNotificationV3 =
                StandaloneNotificationBuilder.buildStandaloneNotification(notificationReportee, commonNotification);
        StandaloneNotification s = sendStandaloneNotificationV3.getStandaloneNotifications().getStandaloneNotification().get(0);

        ReceiverEndPoint receiver = s.getReceiverEndPoints().getValue().getReceiverEndPoint().get(0);
        assertEquals("EmailPreferred", receiver.getTransportType().getValue().value());
        assertEquals(null, receiver.getReceiverAddress().getValue());
    }

    @Test
    public void test_buildStandaloneNotification_norsk_smsPreferred() {
        commonNotification.setTransportType("SMSPreferred");
        notificationReportee.setReceiverAddress("97719925");
        SendStandaloneNotificationBasicV3 sendStandaloneNotificationV3 =
                StandaloneNotificationBuilder.buildStandaloneNotification(notificationReportee, commonNotification);
        StandaloneNotification s = sendStandaloneNotificationV3.getStandaloneNotifications().getStandaloneNotification().get(0);

        ReceiverEndPoint receiver = s.getReceiverEndPoints().getValue().getReceiverEndPoint().get(0);
        assertEquals("SMSPreferred", receiver.getTransportType().getValue().value());
        assertEquals(null, receiver.getReceiverAddress().getValue());
    }

    @Test
    public void test_buildStandaloneNotification_illegalTransporType() {
        commonNotification.setTransportType("TULL");
        notificationReportee.setReceiverAddress("97719925");
        SendStandaloneNotificationBasicV3 sendStandaloneNotificationV3 =
                StandaloneNotificationBuilder.buildStandaloneNotification(notificationReportee, commonNotification);
        StandaloneNotification s = sendStandaloneNotificationV3.getStandaloneNotifications().getStandaloneNotification().get(0);

        ReceiverEndPoint receiver = s.getReceiverEndPoints().getValue().getReceiverEndPoint().get(0);
        assertEquals("Both", receiver.getTransportType().getValue().value());
        assertEquals(null, receiver.getReceiverAddress().getValue());
    }

    @Test
    public void test_buildStandaloneNotification_nynorsk() {
        notificationReportee.setLanguageCode(LanguageCode.NYNORSK);
        SendStandaloneNotificationBasicV3 sendStandaloneNotificationV3 =
                StandaloneNotificationBuilder.buildStandaloneNotification(notificationReportee, commonNotification);
        StandaloneNotification s = sendStandaloneNotificationV3.getStandaloneNotifications().getStandaloneNotification().get(0);
        assertEquals(2068, s.getLanguageID().intValue());
    }
}