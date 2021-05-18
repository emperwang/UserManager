package com.test.wk.util;

import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 *  此报错就跟主机名认证有关
 * 用HttpClient发送HTTPS请求报SSLException: Certificate for <域名>
 *      doesn't match any of the subject alternative names
 */
public class ClientFactory {

    public static final String KEY_TYPE_PKCS = "PKCS12";
    public static final String KEY_TYPE_JKS = "JKS";

    private static boolean shutdown = false;
    /*
        连接池的使用
     */
    private static PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(60, TimeUnit.SECONDS);
    public static CloseableHttpClient CLIENT;
    static {
        pool.setMaxTotal(20);//连接池的最大连接数
        pool.setDefaultMaxPerRoute(200);//每个Rount(远程)请求最大的连接数。
        pool.closeExpiredConnections();
        pool.closeIdleConnections(60, TimeUnit.SECONDS);
    }
    /**
     *  http请求客户端
     * @param
     * @return
     */
    public static CloseableHttpClient httpClient(Integer connectTimeout,Integer socketTime){
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTime).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        return httpClient;
    }

    public static CloseableHttpClient httpClientPooled(Integer connectTimeout,Integer socketTime){
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTime).build();
        if (CLIENT == null){
            CLIENT = HttpClients.custom().setConnectionManager(pool)
                    .evictIdleConnections(10,TimeUnit.SECONDS)
                    .evictExpiredConnections()
                    .setDefaultRequestConfig(requestConfig).build();
        }
        return CLIENT;
    }

}
