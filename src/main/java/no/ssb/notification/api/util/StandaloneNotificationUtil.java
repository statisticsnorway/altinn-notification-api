package no.ssb.notification.api.util;

import no.altinn.services.serviceengine.notification._2010._10.SendStandaloneNotificationBasicV3;
/**
 * Created by rsa on 04.10.2019.
 */
public class StandaloneNotificationUtil {
    public static String standaloneNotificationV3AsString(SendStandaloneNotificationBasicV3 notification) {
        StringBuilder sb = new StringBuilder();

        notification.getStandaloneNotifications().getStandaloneNotification().forEach(n -> {
            sb.append("\n  notification: notificationType=");
            sb.append(n.getNotificationType() == null ? "null" : n.getNotificationType().getValue());
            sb.append(", lang=");
            sb.append(n.getLanguageID() == null ? "null" : n.getLanguageID());
            sb.append(", from=");
            sb.append(n.getFromAddress() == null ? "null" : n.getFromAddress().getValue());
            sb.append(", shipDate=");
            sb.append(n.getShipmentDateTime() == null ? "null" : n.getShipmentDateTime().getDay() + n.getShipmentDateTime().getMonth() + n.getShipmentDateTime().getYear());
            sb.append(", service=");
            sb.append(n.getService() == null ? "null" : n.getService().getValue());
            sb.append(", reporteeNumber=");
            sb.append(n.getReporteeNumber() == null ? "null" : n.getReporteeNumber().getValue());
            sb.append(", isReservable=");
            sb.append(n.getIsReservable() == null ? "null" : n.getIsReservable().getValue());
            sb.append(", roles=");
            sb.append(n.getRoles() == null ? "null" : n.getRoles().getValue());
            sb.append(", useServiceOwnerShortNameAsSenderOfSms=");
            sb.append(n.getUseServiceOwnerShortNameAsSenderOfSms() == null ? "null" : n.getUseServiceOwnerShortNameAsSenderOfSms().getValue());

            n.getReceiverEndPoints().getValue().getReceiverEndPoint().forEach(p -> {
                sb.append("\n    receiver:  address=");
                sb.append(p.getReceiverAddress() == null ? "null" : p.getReceiverAddress().getValue());
                sb.append(", transportType=");
                sb.append(p.getTransportType() == null ? "null" : (p.getTransportType().getValue() + "(" + p.getTransportType().getName() + ")"));
            });
            n.getTextTokens().getValue().getTextToken().forEach(t -> {
                sb.append("\n    textTokens: num=");
                sb.append(t.getTokenNum());
                sb.append(", value=");
                sb.append(t.getTokenValue() == null ? "null" : t.getTokenValue().getValue());
            });
        });

        return sb.toString();
    }
}
