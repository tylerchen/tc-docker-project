/*******************************************************************************
 * Copyright (c) 2020-05-18 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.config;

import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.SSLConfig;
import com.github.dockerjava.core.util.CertificateUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.net.URI;
import java.security.*;
import java.util.Objects;

/**
 * DockerConfig
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-18
 */
@Slf4j
@Data
@Configuration
public class DockerConfig {

    @Value("${config.docker.host:unix:///var/run/docker.sock}")
    private String host;
    @Value("${config.docker.api-version:1.30}")
    private String apiVersion;
    @Value("${config.docker.register-url:https://index.docker.io/v1/}")
    private String registerUrl;
    @Value("${config.docker.register-username:username}")
    private String registerUsername;
    @Value("${config.docker.register-password:password}")
    private String registerPassword;
    @Value("${config.docker.register-email:email}")
    private String registerEmail;
    @Value("${config.docker.compose-dir:/opt/compose}")
    private String composeDir;
    @Value("${config.docker.data-dir:/data}")
    private String dataDir;
    @Value("${config.docker.ssl-enable:false}")
    private Boolean sslEnable;
    @Value("${config.docker.ssl-ca}")
    private String sslCa;
    @Value("${config.docker.ssl-key}")
    private String sslKey;
    @Value("${config.docker.ssl-cert}")
    private String sslCert;
    @Value("${config.docker.server.enable:false}")
    private Boolean serverEnable;

    public static SSLConfig sslConfig(String sslCa, String sslKey, String sslCert) {
        return new SSLConfig() {
            public SSLContext getSSLContext() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
                try {
                    Security.addProvider(new BouncyCastleProvider());
                    String keyPem = Objects.requireNonNull(sslKey);
                    String certPem = Objects.requireNonNull(sslCert);
                    String caPem = Objects.requireNonNull(sslCa);

                    String kmfAlgorithm = AccessController.doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                            return KeyManagerFactory.getDefaultAlgorithm();
                        }
                    });
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(kmfAlgorithm);
                    keyManagerFactory.init(CertificateUtils.createKeyStore(keyPem, certPem), "docker".toCharArray());

                    String tmfAlgorithm = AccessController.doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                            return TrustManagerFactory.getDefaultAlgorithm();
                        }
                    });
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
                    trustManagerFactory.init(CertificateUtils.createTrustStore(caPem));

                    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                    sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                    return sslContext;

                } catch (Exception e) {
                    throw new DockerClientException(e.getMessage(), e);
                }
            }
        };
    }

    @Bean
    public DockerClientConfig config() {
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        builder
                .withDockerHost(host)
                .withApiVersion(StringUtils.defaultString(apiVersion, "1.30")) // optional
                .withRegistryUrl(StringUtils.defaultString(registerUrl, "https://index.docker.io/v1/"))
                .withRegistryUsername(StringUtils.defaultString(registerUsername, "username"))
                .withRegistryPassword(StringUtils.defaultString(registerPassword, "password"))
                .withRegistryEmail(StringUtils.defaultString(registerEmail, "email"));
        if (sslEnable != null && sslEnable) {
            builder.withDockerTlsVerify(true)
                    .withCustomSslConfig(sslConfig(sslCa, sslKey, sslCert));
        }
        return builder.build();
    }
}
