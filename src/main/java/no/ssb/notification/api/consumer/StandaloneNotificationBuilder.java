package no.ssb.notification.api.consumer;

import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType;
import no.altinn.schemas.services.serviceengine.notification._2009._10.*;
import no.altinn.schemas.services.serviceengine.standalonenotificationbe._2009._10.StandaloneNotificationBEList;
import no.altinn.schemas.services.serviceengine.standalonenotificationbe._2015._06.Service;
import no.altinn.services.serviceengine.notification._2010._10.SendStandaloneNotificationBasicV3;
import no.ssb.domene.altinn.correspondence.CommonNotification;
import no.ssb.domene.altinn.correspondence.NotificationReportee;
import no.ssb.notification.api.factory.AltinnObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by rsa on 01.10.2019.
 */
public class StandaloneNotificationBuilder {
    private static final Logger logger = LoggerFactory.getLogger(StandaloneNotificationBuilder.class);

    private static AltinnObjectFactory altinnObjectFactory = new AltinnObjectFactory();

    private static String serviceCodeReplaceString = "$servicename|SC:#|SEC:#$";

    public static SendStandaloneNotificationBasicV3 buildStandaloneNotification(
            NotificationReportee notificationReportee, CommonNotification commonNotification) {
        SendStandaloneNotificationBasicV3 sendStandaloneNotificationV3 = altinnObjectFactory.createSendStandaloneNotificationV3();
        sendStandaloneNotificationV3.setStandaloneNotifications(createStandaloneNotificationBEList(notificationReportee, commonNotification));
        return sendStandaloneNotificationV3;
    }

    private static StandaloneNotificationBEList createStandaloneNotificationBEList(
            NotificationReportee notificationReportee, CommonNotification commonNotification) {
        StandaloneNotificationBEList standaloneNotificationBEList = altinnObjectFactory.createStandaloneNotificationBEList();
        standaloneNotificationBEList.getStandaloneNotification().add(createStandaloneNotification(notificationReportee, commonNotification));
        return standaloneNotificationBEList;
    }


    private static StandaloneNotification createStandaloneNotification(NotificationReportee notificationReportee, CommonNotification commonNotification) {
        StandaloneNotification notification = new StandaloneNotification();
        notification.setFromAddress(altinnObjectFactory.createStandaloneNotificationFromAddress(commonNotification.getFromAddress()));
        notification.setIsReservable(altinnObjectFactory.createBoolean(Boolean.FALSE));
        notification.setLanguageID(new Integer(notificationReportee.getLanguageCode().value()));
        notification.setNotificationType(altinnObjectFactory.createStandaloneNotificationNotificationType(commonNotification.getNotificationType()));
        notification.setReceiverEndPoints(altinnObjectFactory.createStandaloneNotificationReceiverEndPoints(
                createReceiverEndPointBEList(commonNotification.getTransportType(), notificationReportee.getReceiverAddress())));
        notification.setReporteeNumber(altinnObjectFactory.createStandaloneNotificationReporteeNumber(notificationReportee.getReportee()));
        notification.setService(altinnObjectFactory.createStandaloneNotificationService(
                createService(notificationReportee.getServiceCode(), notificationReportee.getServiceEditionCode())));//, altinnNotification.getServiceEdition()));
        notification.setShipmentDateTime(createXmlGregorianDate(commonNotification.getSendDateTime()));


        notification.setTextTokens(altinnObjectFactory.createTextTokenSubstitutionBEList(new TextTokenSubstitutionBEList()));
        TextTokenSubstitutionBEList textTokens = createTextTokens(notificationReportee);
        notification.setTextTokens(altinnObjectFactory.createStandaloneNotificationTextTokens(textTokens));
        return notification;
    }

    private static XMLGregorianCalendar createXmlGregorianDate(LocalDateTime sendDateTime) {
        try {
            logger.info("sendDateTime: " + sendDateTime.toString());
            logger.info(" as xMLGregorieanCalendar: " + DatatypeFactory.newInstance().newXMLGregorianCalendar(sendDateTime.format(DateTimeFormatter.ISO_DATE_TIME)));
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(sendDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        } catch (DatatypeConfigurationException e) {
            return null;
        }
    }

    private static ReceiverEndPointBEList createReceiverEndPointBEList(String transportType, String address)  {
        ReceiverEndPointBEList receiverEndPointBEList = altinnObjectFactory.createReceiverEndPointBEList();
        ReceiverEndPoint receiverEndPoint = altinnObjectFactory.createReceiverEndPoint();

        TransportType altinnTransportType = getTransportType(transportType);
        logger.info("altinn transportationType: {}, address: {}", altinnTransportType.value(), address);
        receiverEndPoint.setTransportType(altinnObjectFactory.createReceiverEndPointTransportType(altinnTransportType));
        receiverEndPoint.setReceiverAddress(altinnObjectFactory.createReceiverEndPointReceiverAddress(
                (TransportType.SMS.equals(altinnTransportType) || TransportType.EMAIL.equals(altinnTransportType)) ?
                        address : null));

        logger.info(" receiverAddress: {}", receiverEndPoint.getReceiverAddress().getValue());
        receiverEndPointBEList.getReceiverEndPoint().add(receiverEndPoint);

        receiverEndPointBEList.getReceiverEndPoint().forEach(r ->
            logger.info("receiver: address={}, transportationType={}", r.getReceiverAddress().getValue(), r.getTransportType().getValue()));
        return receiverEndPointBEList;
    }

    private static TransportType getTransportType(String transportType) {
        logger.info("transportationType: {}", transportType);
        try {
            return !transportType.isEmpty() ? TransportType.fromValue(transportType) : TransportType.BOTH;
        }  catch (IllegalArgumentException iae) {
            return TransportType.BOTH;
        }
    }

    private static TextTokenSubstitutionBEList createTextTokens(NotificationReportee notificationReportee) {
        String sc = notificationReportee.getServiceCode();
        String sec = notificationReportee.getServiceEditionCode();
        TextTokenSubstitutionBEList textTokenSubstitutionBEList = altinnObjectFactory.createTextTokenSubstitutionBEList();
        textTokenSubstitutionBEList.getTextToken().add(createTextToken(0, replaceServiceCodeText(notificationReportee.getNotificationSMS(),sc, sec)));// commonNotification.getNotificationSMS()));
        textTokenSubstitutionBEList.getTextToken().add(createTextToken(1, replaceServiceCodeText(notificationReportee.getNotificationSubject(), sc, sec)));// commonNotification.getNotificationSubject()));
        textTokenSubstitutionBEList.getTextToken().add(createTextToken(2, replaceServiceCodeText(notificationReportee.getNotificationText(), sc, sec)));// commonNotification.getNotificationText()));
        return textTokenSubstitutionBEList;
    }

    private static String replaceServiceCodeText(String notificationText, String serviceCode, String serviceEditionCode) {
        return notificationText.replace(serviceCodeReplaceString, "$servicename|SC:" + serviceCode + "|SEC:" + serviceEditionCode + "$");
    }

    private static TextToken createTextToken(int tokenNum, String tokenValue) {
        TextToken textToken = new TextToken();
        textToken.setTokenNum(tokenNum);
        textToken.setTokenValue(altinnObjectFactory.createTextTokenTokenValue(tokenValue != null ? tokenValue :""));
        return textToken;
    }

    private static Service createService(String serviceCode, String serviceEditionCode) {
        Service service = new Service();
        service.setServiceCode(serviceCode);
        service.setServiceEdition(Integer.valueOf(serviceEditionCode));
        return service;
    }

}
