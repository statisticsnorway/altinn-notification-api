package no.ssb.notification.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.altinn.schemas.services.serviceengine.notification._2009._10.StandaloneNotification;
import no.altinn.schemas.services.serviceengine.notification._2015._06.NotificationResult;
import no.altinn.schemas.services.serviceengine.notification._2015._06.SendNotificationResultList;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage;
import no.altinn.services.serviceengine.notification._2010._10.SendStandaloneNotificationBasicV3;
import no.ssb.notification.api.consumer.StandaloneNotificationExternalBasicService;
import no.ssb.domene.altinn.correspondence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rsa on 04.10.2019.
 */
@Api("Altinn-Notification-api")
@RestController
@RequestMapping("")
public class StandaloneNotificationController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String HEADER_APIKEY = "X-SSB-APIKEY";
    @Value("${api-keys}")
    private List<String> acceptedApiKeys;

    @Autowired
    private StandaloneNotificationExternalBasicService standaloneNotificationExternalBasicService;


    @ApiOperation("Sende varsling til Altinn")
    @RequestMapping(value = "/standalonenotification", method = RequestMethod.POST)
    public ResponseEntity<?> sendStandaloneNotification(@RequestBody AltinnNotification altinnNotification, @RequestHeader HttpHeaders header) {
        if (!authorizeRequest(header)) {
            return new ResponseEntity<>("Unauthorized. Api-key er ugyldig eller mangler", HttpStatus.UNAUTHORIZED);
        }
        logger.info("Sending altinnMessage for: {} \n {}", altinnNotification.getReportees().size(),
                altinnNotification.getCommonNotification().getNotificationType());
        StandaloneNotificationResponseList standaloneNotificationResponseList = new StandaloneNotificationResponseList();

        altinnNotification.getReportees().stream().forEach(reportee -> {
            standaloneNotificationResponseList.addAllStandaloneNotificationResponse(sendSingleStandaloneNotification(reportee, altinnNotification.getCommonNotification()));
        });
        return new ResponseEntity<>(standaloneNotificationResponseList, HttpStatus.OK);
    }




     private StandaloneNotificationResponseList sendSingleStandaloneNotification(NotificationReportee notificationReportee, CommonNotification commonNotification) {
         StandaloneNotificationResponseList standaloneNotificationResponseList = new StandaloneNotificationResponseList();

        try {
            SendNotificationResultList sendNotificationResultList = standaloneNotificationExternalBasicService.sendNotification(notificationReportee, commonNotification);
            standaloneNotificationResponseList = createStandaloneNotificationResponse(sendNotificationResultList);
            logResponsAsJson(sendNotificationResultList);
            logger.info("Sendt altinnNotification for {}", notificationReportee.toString());
        } catch (INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage errorResult) {
            logResponseError(errorResult);
//            CorrespondenceResponse response = new CorrespondenceResponse();
//            response.setReceiptStatusCode(CorrespondenceReceiptStatusCode.UNEXPECTED_ERROR);
//            response.setReceiptStatusText(iNotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage.getFaultInfo().getAltinnErrorMessage().getValue());
            standaloneNotificationResponseList = createStandaloneNotificationErrorResponse(notificationReportee, commonNotification, errorResult);
            //TODO: Hva skal skje med feil-responsen?
        }
         return standaloneNotificationResponseList;


    }


    private StandaloneNotificationResponseList createStandaloneNotificationResponse(SendNotificationResultList resultList) {

        StandaloneNotificationResponseList standaloneNotificationResponseList = new StandaloneNotificationResponseList();

        resultList.getNotificationResult().stream().forEach(r -> {
            StandaloneNotificationResponse standaloneNotificationResponse = new StandaloneNotificationResponse();
            standaloneNotificationResponse.setNotificationType(r.getNotificationType().getValue());
            standaloneNotificationResponse.setReporteeNumber(r.getReporteeNumber().getValue());
            logger.info("Response: {}, {} ", r.getNotificationType().getValue(), r.getReporteeNumber().getValue());
            r.getEndPoints().getValue().getEndPointResult().forEach(epr -> {
                logger.info(" endpoint-result: {}", epr.getReceiverAddress().getValue(), epr.getName().getValue(), epr.getTransportType().value());
                StandaloneNotificationEndpointResponse endpointResponse = new StandaloneNotificationEndpointResponse();
                endpointResponse.setReceiverAddress(epr.getReceiverAddress().getValue());
                endpointResponse.setName(epr.getName().getValue());
                endpointResponse.setTransportType(epr.getTransportType().value());
                endpointResponse.setRetrieveFromProfile((epr.getRetrieveFromProfile() != null && epr.getRetrieveFromProfile().getValue() != null && epr.getRetrieveFromProfile().getValue().booleanValue()) ? "retrieveFromProfile" : "");
                standaloneNotificationResponse.getEndpointResponses().add(endpointResponse);
                standaloneNotificationResponse.setResponseCode(CorrespondenceReceiptStatusCode.OK.name());
                logger.info("created response: " + standaloneNotificationResponse.toString());
            });
            standaloneNotificationResponseList.addStandaloneNotificationResponse(standaloneNotificationResponse);

        });

        return standaloneNotificationResponseList;
    }


    private StandaloneNotificationResponseList createStandaloneNotificationErrorResponse(NotificationReportee notificationReportee, CommonNotification commonNotification,
                                                                                     INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage errorResult) {
        logErrorResponsesAsJson(errorResult);
        StandaloneNotificationResponseList standaloneNotificationResponseList = new StandaloneNotificationResponseList();

        StandaloneNotificationResponse standaloneNotificationResponse = new StandaloneNotificationResponse();
        standaloneNotificationResponse.setReporteeNumber(notificationReportee.getReportee());
        standaloneNotificationResponse.setNotificationType(commonNotification.getNotificationType());

        StandaloneNotificationEndpointResponse endpointResponse = new StandaloneNotificationEndpointResponse();
        endpointResponse.setReceiverAddress(notificationReportee.getReceiverAddress());
        endpointResponse.setTransportType(commonNotification.getTransportType());
        standaloneNotificationResponse.getEndpointResponses().add(endpointResponse);

        standaloneNotificationResponse.setResponseCode(errorResult.getFaultInfo().getErrorID().toString());
        standaloneNotificationResponse.setErrorMessage(errorResult.getFaultInfo().getAltinnLocalizedErrorMessage().getValue());
        logger.info("created response: " + standaloneNotificationResponse.toString());
        standaloneNotificationResponseList.addStandaloneNotificationResponse(standaloneNotificationResponse);
        return standaloneNotificationResponseList;
    }

    private boolean authorizeRequest(HttpHeaders header) {
        if (header.get(HEADER_APIKEY) == null) {
            return false;
        }
        return acceptedApiKeys.contains(header.get(HEADER_APIKEY).get(0));
    }



    private void logResponsAsJson(SendNotificationResultList sendNotificationResultList) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonCorrespondence = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sendNotificationResultList);
            logger.debug("Notification: \n{}", jsonCorrespondence);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void logErrorResponsesAsJson(INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage errorResult) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonCorrespondence = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorResult);
            logger.debug("Notification: \n{}", jsonCorrespondence);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    private void logResponseError(INotificationAgencyExternalBasicSendStandaloneNotificationBasicV3AltinnFaultFaultFaultMessage notificationFaultMessage) {
        logger.error(notificationFaultMessage.getMessage());
        logger.error("ErrorID: " + notificationFaultMessage.getFaultInfo().getErrorID());
        logger.error("Message : {}",  notificationFaultMessage.getMessage());
        logger.error("AltinnErrorMessage: " + notificationFaultMessage.getFaultInfo().getAltinnErrorMessage().getValue());
        logger.error("AltinnExtendedErrorMessage: " + notificationFaultMessage.getFaultInfo().getAltinnExtendedErrorMessage().getValue());
        logger.error("AltinnLocalizedErrorMessage: " + notificationFaultMessage.getFaultInfo().getAltinnLocalizedErrorMessage().getValue());
    }


}
