package com.sencorsta.ids.core.net.innerServer;

import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

/**
 * @author ICe
 */
@Slf4j
public class SSLHandlerProvider {
    private static final String PROTOCOL = "TLS";
    private static final String ALGORITHM_SUN_X509 = "SunX509";
    private static final String ALGORITHM = "ssl.KeyManagerFactory.algorithm";
    private static final String KEYSTORE = "icesslstore.jks";
    private static final String KEYSTORE_TYPE = "JKS";
    private static final String KEYSTORE_PASSWORD = "123456";
    private static final String CERT_PASSWORD = "123456";
    private static SSLContext serverSSLContext = null;
    private static SslContext serverSSLContext2 = null;

    public static SslHandler getSSLHandler() {
        SSLEngine sslEngine = null;
        if (serverSSLContext == null) {
            log.error("Server SSL context is null");
            System.exit(-1);
        } else {
            sslEngine = serverSSLContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(false);

        }
        return new SslHandler(sslEngine);
    }

    public static OptionalSslHandler getOptionalSslHandler() {
        return new OptionalSslHandler(serverSSLContext2);
    }

    public static void initSSLContext() {

        log.info("Initiating SSL context");
        String algorithm = Security.getProperty(ALGORITHM);
        if (algorithm == null) {
            algorithm = ALGORITHM_SUN_X509;
        }
        KeyStore ks = null;
        InputStream inputStream = null;
        try {
            inputStream = SSLHandlerProvider.class.getClassLoader().getResourceAsStream(KEYSTORE);;
            ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(inputStream, KEYSTORE_PASSWORD.toCharArray());
        } catch (IOException e) {
            log.error("Cannot load the keystore file", e);
        } catch (CertificateException e) {
            log.error("Cannot get the certificate", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Somthing wrong with the SSL algorithm", e);
        } catch (KeyStoreException e) {
            log.error("Cannot initialize keystore", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Cannot close keystore file stream ", e);
            }
        }
        try {
            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, CERT_PASSWORD.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();
            // Setting trust store null since we don't need a CA certificate or Mutual Authentication
            TrustManager[] trustManagers = null;

            serverSSLContext = SSLContext.getInstance(PROTOCOL);
            serverSSLContext.init(keyManagers, trustManagers, null);
        } catch (Exception e) {
            log.error("Failed to initialize the server-side SSLContext", e);
        }
    }

    public static void initSSLContext2() {
        try {
            final String name = "sencorsta.com+5.pem";
            final String nameKey = "sencorsta.com+5-key.pem";
            final ClassLoader classLoader = SSLHandlerProvider.class.getClassLoader();
            InputStream keyCertChainInputStream = classLoader.getResourceAsStream(name);
            InputStream keyInputStream = classLoader.getResourceAsStream(nameKey);
            serverSSLContext2 = SslContextBuilder.forServer(keyCertChainInputStream, keyInputStream).build();
            log.info("HTTPS 证书加载成功 --> " + name);
            log.info("HTTPS 私钥加载成功 --> " + nameKey);
            //临时生成一个证书
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            serverSSLContext2 = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

        } catch (Exception e) {
            log.error("Failed to initialize the server-side SSLContext", e);
        }


    }

    public static void main(String[] args) throws CertificateException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        ssc.certificate();
        ssc.privateKey();
        System.out.println("111");
    }

}
