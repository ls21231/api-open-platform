package com.ls.lapiclientsdk;

import com.ls.lapiclientsdk.client.LSClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Data
@Configuration
@ComponentScan
@ConfigurationProperties("ls.client")
public class LSApiClientConfig {

    private String accessKey;
    private String secretKey;

    @Bean
    public LSClient lsClient() {
        return new LSClient(accessKey,secretKey);
    }

}
