package no.ssb.notification.api.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.altinn.schemas.services.serviceengine.notification._2015._06.SendNotificationResultList;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasic;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage;
import no.altinn.services.serviceengine.notification._2010._10.SendStandaloneNotificationBasicV3;
import no.ssb.domene.altinn.correspondence.CommonNotification;
import no.ssb.domene.altinn.correspondence.NotificationReportee;
import no.ssb.notification.api.factory.AltinnObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by rsa on 01.10.2019.
 */
@Service
public class StandaloneNotificationExternalBasicService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${altinn.notification.systemuser}")
    private String systemUser;
    @Value("${altinn.notification.systemusercode}")
    private String systemUserCode;
    @Value("${altinn.notification.passord}")
    private String passord;


    private AltinnObjectFactory altinnObjectFactory;

    @Autowired
    private INotificationAgencyExternalBasic iNotificationAgencyExternalBasic;

    public StandaloneNotificationExternalBasicService() { altinnObjectFactory = new AltinnObjectFactory(); }


    public SendNotificationResultList sendNotification(NotificationReportee notificationReportee, CommonNotification commonNotification) throws INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage {
        SendStandaloneNotificationBasicV3 sendStandaloneNotificationBasicV3 = StandaloneNotificationBuilder.buildStandaloneNotification(notificationReportee, commonNotification);

//        sendStandaloneNotificationV3.getStandaloneNotifications().getStandaloneNotification().forEach(
//                n -> logger.info("standaloneNotification: {}, transportationtype: {}", n.getReporteeNumber(), n.getReceiverEndPoints()));
        logNotificationAsJson(sendStandaloneNotificationBasicV3);
        logNotification(sendStandaloneNotificationBasicV3);

        return iNotificationAgencyExternalBasic.sendStandaloneNotificationBasicV3(systemUser, passord, sendStandaloneNotificationBasicV3.getStandaloneNotifications());
    }

    private void logNotification(SendStandaloneNotificationBasicV3 sendStandaloneNotificationBasicV3) {
        sendStandaloneNotificationBasicV3.getStandaloneNotifications().getStandaloneNotification().forEach(
                sn -> {
                    logger.info(sn.getReporteeNumber().getValue());
                    logger.info(sn.getFromAddress().getValue());
                    logger.info(sn.getNotificationType().getValue());
                    logger.info(sn.getService().getValue().getServiceCode());
                    logger.info(String.valueOf(sn.getService().getValue().getServiceEdition()));
                    logger.info("endpoints: ");
                    sn.getReceiverEndPoints().getValue().getReceiverEndPoint().forEach( re -> {
                        logger.info("  " + re.getReceiverAddress().getValue());
                        logger.info("  " + re.getTransportType().getValue());
                    });
                    logger.info("---");
                }
        );
    }


    private void logNotificationAsJson(SendStandaloneNotificationBasicV3 notification) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonCorrespondence = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notification);
            logger.debug("Notification: \n{}", jsonCorrespondence);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
