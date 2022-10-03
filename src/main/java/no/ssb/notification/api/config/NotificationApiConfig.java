package no.ssb.notification.api.config;

import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasic;
import no.ssb.notification.api.consumer.StandaloneNotificationExternalBasicService;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rsa on 14.10.2016.
 */
@Configuration
public class NotificationApiConfig {

    @Value("${altinn.notification.url}")
    private String altinnNotificationAgencyUrl;

    @Bean
    public INotificationAgencyExternalBasic iNotificationAgencyExternalBasic() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setAddress(altinnNotificationAgencyUrl);
        factory.setServiceClass(INotificationAgencyExternalBasic.class);
        INotificationAgencyExternalBasic service = (INotificationAgencyExternalBasic) factory.create();
        return service;
    }

    @Bean
    public StandaloneNotificationExternalBasicService standaloneNotificationExternalBasicService() {
        return new StandaloneNotificationExternalBasicService();
    }
}
