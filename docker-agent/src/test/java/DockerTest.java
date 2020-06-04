/*******************************************************************************
 * Copyright (c) 2020-05-26 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/

import com.alibaba.fastjson.JSON;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.SSLConfig;
import com.github.dockerjava.core.util.CertificateUtils;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * DockerTest
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-26
 */
public class DockerTest {

    //    @ClassRule
//    public static DockerComposeContainer environment =
//            new DockerComposeContainer(new File("src/test/resources/compose-test.yml"))
//                    .withExposedService("redis_1", 6306)
//                    .withExposedService("elasticsearch_1", 9200);
    public static void main(String[] args) throws Exception {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withCustomSslConfig(test())
                .withDockerHost("tcp://47.113.118.118:2376")
                .withDockerTlsVerify(true)
                .withApiVersion("1.30") // optional
                .withRegistryUrl("https://index.docker.io/v1/")
                .withRegistryUsername("username").withRegistryPassword("password")
                .withRegistryEmail("email").build();

        Ports portBindings = new Ports();
        portBindings.bind(ExposedPort.tcp(80), Ports.Binding.bindPort(80));

        DockerClient docker = DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(new NettyDockerCmdExecFactory()).build();
        {
            PullImageCmd nginx = docker.pullImageCmd("nginx").withTag("1.17.9");
            PullImageResultCallback res = new PullImageResultCallback();
            res = nginx.exec(res);
            res.awaitCompletion();
            List<Container> list = docker.listContainersCmd().withNameFilter(Arrays.asList("test")).exec();
            if (list.isEmpty()) {
                CreateContainerResponse container = docker.createContainerCmd("nginx:1.17.9").withName("test")
                        .withExposedPorts(ExposedPort.tcp(80))
                        .withHostConfig(HostConfig.newHostConfig().withPortBindings(portBindings))
                        .exec();
                docker.startContainerCmd(container.getId()).exec();
            }
        }
        {
            List<Container> list = docker.listContainersCmd().exec();
            System.out.println("docker containers：=================");
            System.out.println(JSON.toJSONString(list, true));
        }
        {
            List<Image> list = docker.listImagesCmd().exec();
            System.out.println("docker images：=================");
            System.out.println(JSON.toJSONString(list, true));
        }
        {
            Info info = docker.infoCmd().exec();
            System.out.println("docker的环境信息如下：=================");
            System.out.println(JSON.toJSONString(info, true));
        }
        docker.close();
    }

    public static SSLConfig test() {
        String CA = "-----BEGIN CERTIFICATE-----\n" +
                "MIIFizCCA3OgAwIBAgIJAL77o+N1X/p7MA0GCSqGSIb3DQEBCwUAMFwxCzAJBgNV\n" +
                "BAYTAkNOMQswCQYDVQQIDAJHRDELMAkGA1UEBwwCR1oxDDAKBgNVBAoMA0lGRjEM\n" +
                "MAoGA1UECwwDSUZGMRcwFQYDVQQDDA40Ny4xMTMuMTE4LjExODAeFw0yMDA1MjYw\n" +
                "NjM1MjBaFw0yMTA1MjYwNjM1MjBaMFwxCzAJBgNVBAYTAkNOMQswCQYDVQQIDAJH\n" +
                "RDELMAkGA1UEBwwCR1oxDDAKBgNVBAoMA0lGRjEMMAoGA1UECwwDSUZGMRcwFQYD\n" +
                "VQQDDA40Ny4xMTMuMTE4LjExODCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoC\n" +
                "ggIBAL8loe7jhDHN33y3uxv6XeRsKEsqVGr3YbCXAGFh5zColmzr0tYd2UMIT0d8\n" +
                "QdxgW2ljWw13oGg3WRHCbI+nHar+fwkJ02eF3Ya8CVPPuEiMJkHat+qneplejfzn\n" +
                "kTRqw05LCQPG7oUJIh6KrhSIJCZhgueHO3hgycYwW6QScY335b6WIO5YXF5ZycJE\n" +
                "L8HRE/N+JDlTsGgpQhNxt0dr7EC12kWugwq0qh1Hfd9M6HY4871dDiL+3Qo6gHSt\n" +
                "Du1k6jDmjTrENogy/PDDZksW3b0Ct2MxYcs25zqPltHfSvWu03MP1DR/6cuqaPB9\n" +
                "ErwEMyoo5y7I128LAF1bbpPE7sZdwV+AmxufHY9GPDIOvCYn5z8+Iy/bE/wtWG53\n" +
                "AIwu3TPaztveeenYweLoomEvhXEvFR5Ql2HH6A1myH4EZx/92NFBBMOXkIfCVBeT\n" +
                "D9+WLWXXQ72nfFA4aphObb40OgboBYX//tPQ4mOtkRuzCO5zDplp4o/3ylRJ5X7o\n" +
                "dFp7WLkdlad7rpdXUdVrY/RJm5hBXAKHuq2mHVQMtiopT+Z+OSvlE58AqtCgkiwq\n" +
                "PHbdOnbAsoi81Y7rNyw2lMNOPrx0bEqaNQiVQC/Bd3/OcmVLNBw2fJlqhiMQqvkL\n" +
                "PuiVnMhRMWoAFfRhdKZBcVwHqH8E6/1J2bjMb/4CFl8dgqOXAgMBAAGjUDBOMB0G\n" +
                "A1UdDgQWBBR7xGRGbqkeME35gb+FZuk6RZOUojAfBgNVHSMEGDAWgBR7xGRGbqke\n" +
                "ME35gb+FZuk6RZOUojAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4ICAQCf\n" +
                "koR3s8HiJjIEYzyC4Tw2l1VIvnoae8eSYliVP/GmzoP7g0yMfIaChtupXmZot4Q/\n" +
                "Ioqt/5dkol4vUnCbYKeC22vMjY8b+7Owc5P6a7lP04/l66ThIe6vWyr5qH5cJKdy\n" +
                "Z49Xhlm3HMgxCA2guIE4kuKd3DoM0gLz9gcgTPZGPi6jF7AyjAQ+39XiC8vUwbMP\n" +
                "dSp0C9ADBmanvdF8an1r+D+OZFdZsNKF3/4/dEKev1Nj/drqqfkUNSUn3pqm4sip\n" +
                "isKYHLHo+zco+2bcGsloHhXb6LZmJjH+Fhzch1ewprLF4FmBImvdxdn981NuGX4r\n" +
                "KXAqkTcW4YSZnnLM1fcaTCxM0UXU5oOVheVNQwjyhI46NyEQGHETjOyTMOMEarCv\n" +
                "bvR6Mt28sIfv7b0K05QbWrBSQqZfelg60KObc1oHzdwLvU2sByuFMK5DDomoQWnD\n" +
                "pc86WxY+LGUL8ZUK7y7gK3gmUn3Fzacl2i+QetYP4wOMUvU/UmsV4f5Apca+FPjh\n" +
                "fJAC+wGRvxwXQJcEgpfjQjFyHdpaMWyCH7ZTSAH9dXn0RQfSshXCvFHVqqW3psfA\n" +
                "jVkbUWYvQslzU7OntGXuzAk/dwDpc3gH+un5NfQnVQY++I98vZEb7tT7VnwQQfJa\n" +
                "op9X4kU3d2v+0tN481oWLCMNrl1jhXUG8uwLKW85FQ==\n" +
                "-----END CERTIFICATE-----\n";

        String KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIJKgIBAAKCAgEAyQnb4wpnvMC1WP0r/IEhUPedgjvtr0OuuQTl43jZpLnrwLRH\n" +
                "Ou552M0vg1+4f+kYqUPey8t81aI1mzzX1UYauXnxZVihwzDlnCFbN97lBJ4YHuYE\n" +
                "t3umqZrAhXOJ9Yn5FTmxxy09+UVx0+VnW6HjUKbbYrn3yU+yrYKwwcCrq7CCkVrn\n" +
                "xdD9rvftE84aWP/6HL3VhGO7q8MafCRDsypGOT9l/Y1X42MOEE4mFs6lT+VY1ZCF\n" +
                "kpACrJ0HIunN/GWvLT9VRl77Pwcybh1XeUvuodq1ypPuIr3FqKdnttcsgbHcWChP\n" +
                "DFNdkVHr+v/EW7gvWOH7YtilwLUAJp5ElHx9ZFBYRhbrVFYNLfk5zEZQvpC1dl15\n" +
                "otTeNrRJjqi0YfuJkYoYsN9AWZKTjfGUwHxCJ85Dbd0ATzZn+h43l9iInA6UWi1W\n" +
                "1oCiOktU+9CoOtFtnwrFi2uTMueF09qPNTXNK9kq3KuY9D8odv3HUhJJY5YMnkhB\n" +
                "52fy6+kiF1JYK08N8zTcVl76fInKisnZTVrMCnCjRss1E7V+xQf9gk7NnUFSTuUv\n" +
                "/YHF41xRQ/f5pOwuhYYYQSl7lwNEMOg32f5FbHF9f9UioC3Y7HMDhtAOsR1MiX+i\n" +
                "OX1rhpt8HkZ66ItwL8sO/Wt5QFGw1XeysuIbYZ6lIUHXdpW5oXHLzIHhfhMCAwEA\n" +
                "AQKCAgBj/kBdZC6G5r8MnRhExqGgmRjBzzcApsSACFyj0NdIJKWLOt/3AuieGiiY\n" +
                "yoKFNJZsknJ9oR/dToolRZUA4pamTG3x/6yt3mNGTh0F5Bq7Ojs7GyObc6hI0c0S\n" +
                "U456D39RvFmdfPZHRvA/+el33USoN+YE7ATAH6D6xndOV5vLkMbQr4t6i+GyJmTI\n" +
                "Jgpa3C82HoHWEJkm7Kr+tjbqJYu6K7t56tSdKAa1u3kcfPoijCusRe76ICEEZDJJ\n" +
                "fktTT+xasBalItcK2XSO6o84qLjvzk3/jaazpvpqUaG3gr0DYUj8nEf9FHYEst+R\n" +
                "/6dWC32ZUwVre+CtwhlqG+x2gpkG6xdTMHM+D3uuAMzUKCoxTBmSItoqBL45Z0SK\n" +
                "fyI6EMh6GshYtSElN3EWTEdCtLoe2E/WoZ7NW5zXj5ty2xxQVtjmeiCCp0AJCDKH\n" +
                "3a9Ht9TdbgQTTI4TmPDIl8r4DNI4luKQGmPaoYtrUs73TyIL8/BL//gKUIAIfKXv\n" +
                "ZEmwB/Z09DpOwIMHU6wP8yLf1CZ/d1DaCj/YcOQAqx+KWLb/fMXkT1VZCp2valac\n" +
                "I7tIgg3xN6ZNXKLpairSHDVaQyVSNzi/5nKpFORbFYtIQ/fLZahEzo76RMYcLaGA\n" +
                "yghn16RUtmQiFl1yehbteOUu6JOfHbmMkZv22+2f/9Hmm+aCQQKCAQEA8D4nCdzl\n" +
                "j388bv7ue55bv6FaVyhbadJhkUJjvU3Uax/WcpabeCd1nZPOjz8gS8U/eB0LT1IT\n" +
                "PDzABwzLG2OrrNguXEJK5/KAyiLSQkizzaU1E70CLO1o97KoX/dLJQRlkW8Jg/hU\n" +
                "vG4iF5yYX9P2qGA1NDnK1tuTkmc9sAFm9PK5Y0RywYe6m3yaUlTs2GF3s3Hr7Lp8\n" +
                "RR+QJ1ulRX8QkExFaK7eQNML/stBduejF/q3U02WU01caBn/AqrWDcv8zw24CXvS\n" +
                "tF/ums4kqNdnhwLyAdbqbQ/zqiMuaUu4V+VgYmW7eOIZcFJTzv5NFoR7ees/YZkm\n" +
                "GU/13prQ6yfFoQKCAQEA1jlwVBZ2e2YPSQISAHWCkOilfR1iWTWEiFynmJyBPe50\n" +
                "oNqqw67fogxStGopqCkhRViRtcLppVmDlHIlIBrN8WegOIjHUPKCKMrvhmqbAkU6\n" +
                "L3DiA/qpA88a+2be3DwHbo9j0Pr0JP8C6i+D3FapqNXysVgw+0iEDcSIfX1hTHoK\n" +
                "pza/4hLgKUn16GOErrERryf3na5BE4ZH9d/S3zehq4HozmU1Nls8xPIf+8UUlJPo\n" +
                "CiFOjwafl+tjb6ydNj14zZmDTcF+EkpFehrvMyUakJCdchgcAqaBJBVavOOniuEk\n" +
                "CETKxmMHkHWbZXE6dCsMrYNKW4Hr3jVZ0xaLaUK/MwKCAQEA5YrdDgVrqXHusjcO\n" +
                "cjraevyWLtS2kyOaaYo/di6ZGUmwQog4P9OkA02ofSHZn8v1WrSVi9Jl4pZA+/tz\n" +
                "TNYsv9dGZIxKudfwyjH/J6oDcie50QQ0weyM8K5ZRmgn+lR7HzwyoU9y1x97n4fV\n" +
                "5ynFcReLfj0B28ys8aOHQ1Xzo3MPQxOxecxPtiSV5riSHCT0lBXzT5rXBcyklZZx\n" +
                "ETfZAaZ+YwlB/jooKejWwd+M62p4IzXsvecatbSw/UFxiHkxMjxvbVBMAF/wwSTZ\n" +
                "ziESH+Pmi29nSZGGRTecLqtUlVbqgtmlCFkv6SwgO5Sq3yryN4lYY85KosDNsUXG\n" +
                "/AhiQQKCAQEAwe6Tuvvu3L1C/yPpqhK3VXnXPtGYFAx3436FlomwEqHJYUkzqByq\n" +
                "hSfEw2EMPxik7wNEPZirLa5AiOVgqJS7dVwLVsK0NZqkrEsy1auykjabexEk+tcz\n" +
                "pKgb1BWHkTaiv7r9cNWOqeNV/y1uoMyvoJG2uEePaBx4t4IA0iHsMHCXLlNJGF9g\n" +
                "IOC8xlk5Z3ATA08aQdvbO9KnWHX8j2jsabgcSqSirIeiFuTegW0juXHAcVM/rAkh\n" +
                "90T36sQIOm8h6nabwIt3FxW3mdfCTbm8N71DW2NqEBgt9r2CPFeXeAljNj0YmMpT\n" +
                "sbpWKFp9wWarfnTJnteZ8Lq5i3fwUyVCswKCAQEAhjjpxfS9eXeXNwsFyaaDzI7n\n" +
                "h8r3l/IB4D/ejFf/on4FtArgtDWP9iolghGAS2QmuaMYnxUey3MdWJFhXezguQqp\n" +
                "Fo064jJaVRKasCb+ErdgzOPncOHr7Xg4HZoV70cFqfF8dGh3EYl7u7DVH1yEsMxm\n" +
                "63jLXnO9uyR5hBrmDnBWH8zV3dQ4+PnLNg/FF8Huc3xw7RiQXzcAS+V8a3uXAhuq\n" +
                "+E0If9WZ4jC4EnvLQJdqokq9E1jdq03IxDI8LHWR5DM0e3svxiTR7BHk3/T4zgK9\n" +
                "PrgNX5t+exRy9tqnt/o5QfmFMLXE0ZGDxgDXrT5eOwzcR0hvKV4FJr/694cJXw==\n" +
                "-----END RSA PRIVATE KEY-----\n";

        String CERT = "-----BEGIN CERTIFICATE-----\n" +
                "MIIFDzCCAvegAwIBAgIJALAtZjzpDLAsMA0GCSqGSIb3DQEBCwUAMFwxCzAJBgNV\n" +
                "BAYTAkNOMQswCQYDVQQIDAJHRDELMAkGA1UEBwwCR1oxDDAKBgNVBAoMA0lGRjEM\n" +
                "MAoGA1UECwwDSUZGMRcwFQYDVQQDDA40Ny4xMTMuMTE4LjExODAeFw0yMDA1MjYw\n" +
                "NjM4MjdaFw0yMTA1MjYwNjM4MjdaMBExDzANBgNVBAMMBmNsaWVudDCCAiIwDQYJ\n" +
                "KoZIhvcNAQEBBQADggIPADCCAgoCggIBAMkJ2+MKZ7zAtVj9K/yBIVD3nYI77a9D\n" +
                "rrkE5eN42aS568C0RzruedjNL4NfuH/pGKlD3svLfNWiNZs819VGGrl58WVYocMw\n" +
                "5ZwhWzfe5QSeGB7mBLd7pqmawIVzifWJ+RU5scctPflFcdPlZ1uh41Cm22K598lP\n" +
                "sq2CsMHAq6uwgpFa58XQ/a737RPOGlj/+hy91YRju6vDGnwkQ7MqRjk/Zf2NV+Nj\n" +
                "DhBOJhbOpU/lWNWQhZKQAqydByLpzfxlry0/VUZe+z8HMm4dV3lL7qHatcqT7iK9\n" +
                "xainZ7bXLIGx3FgoTwxTXZFR6/r/xFu4L1jh+2LYpcC1ACaeRJR8fWRQWEYW61RW\n" +
                "DS35OcxGUL6QtXZdeaLU3ja0SY6otGH7iZGKGLDfQFmSk43xlMB8QifOQ23dAE82\n" +
                "Z/oeN5fYiJwOlFotVtaAojpLVPvQqDrRbZ8KxYtrkzLnhdPajzU1zSvZKtyrmPQ/\n" +
                "KHb9x1ISSWOWDJ5IQedn8uvpIhdSWCtPDfM03FZe+nyJyorJ2U1azApwo0bLNRO1\n" +
                "fsUH/YJOzZ1BUk7lL/2BxeNcUUP3+aTsLoWGGEEpe5cDRDDoN9n+RWxxfX/VIqAt\n" +
                "2OxzA4bQDrEdTIl/ojl9a4abfB5GeuiLcC/LDv1reUBRsNV3srLiG2GepSFB13aV\n" +
                "uaFxy8yB4X4TAgMBAAGjHzAdMBsGA1UdEQQUMBKHBC9xdnaHBKwS3lOHBH8AAAEw\n" +
                "DQYJKoZIhvcNAQELBQADggIBAGXKqb5zP9l0jp2j1o3nKPK64cOjbkMhkaLnQvIZ\n" +
                "9ECArqzVDxnaE1G4pzLxoqzIQpmJGIDiDUxTL/D2tWdo0KkGF4nuwhERDXnqINdv\n" +
                "3LZWeQhhj6ALXhr0dDTMEcuUs+/pq+FprRJGxCgkLAazT0hirbDFfmNTYyeYErIk\n" +
                "zRF5LgCM1uZCenf0Ib8etROeX5CeSXfjWq5ERTGPraKm90oocrvOwn2lZdWoO0b6\n" +
                "vjyrzBPYYToEQgRNYGPpMu4Fg+J4KXH9GHqrgX4llZf8vtHp0ZXd6QXxXZjS9ozC\n" +
                "7oqHyfLLDLRryGheRgzOdG0mwfG1HiJC9Wdv2OB63cv+QhcvtEBjNlNqLRi9Zv8L\n" +
                "oA5TS7Psu8P8sxpD28Wk28VxvhGTOJrvC7qkTVi6AORHeVvfbY6xwqxRGo550tJi\n" +
                "OIIv+/DCc13WNlBNGnuHslkpsNT8f2t2sVfsQM6h/ntR5yIRczJDEm72UAJAqvwM\n" +
                "pRXAhAURmi64WxwBq83muoB1VsXxO/3McPWP2AQ/mmQwaZt3f14Ow4FEjZi0uuI6\n" +
                "QX1gYj9maKTTN2/cyM3JRcOy+/lWCst4Kaw1bfPse31DihJk1xOWWLSb5AsMN0zp\n" +
                "NFtY4csij5egI3hvGJaYZY62LEbNi7n0QJXCYhKPEmEMHrXGpE2uLQ4s641iABzf\n" +
                "6vCE\n" +
                "-----END CERTIFICATE-----\n";
        return new SSLConfig() {
            public SSLContext getSSLContext() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
                try {
                    Security.addProvider(new BouncyCastleProvider());
                    String keypem = KEY;
                    String certpem = CERT;
                    String capem = CA;

                    String kmfAlgorithm = AccessController.doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                            return KeyManagerFactory.getDefaultAlgorithm();
                        }
                    });
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(kmfAlgorithm);
                    keyManagerFactory.init(CertificateUtils.createKeyStore(keypem, certpem), "docker".toCharArray());

                    String tmfAlgorithm = AccessController.doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                            return TrustManagerFactory.getDefaultAlgorithm();
                        }
                    });
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
                    trustManagerFactory.init(CertificateUtils.createTrustStore(capem));

                    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                    sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                    return sslContext;

                } catch (Exception e) {
                    throw new DockerClientException(e.getMessage(), e);
                }
            }
        };
    }
}
