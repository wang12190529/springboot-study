package com.baili.springboot.core.util.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Created by wxl
 *
 */
public class HttpClientUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtils.class);

    private static final CloseableHttpClient defaultHttpClient = HttpClientUtils.createDefaultHttpClient();

    /**
     * connection超时时间
     */
    private static int DEFAULT_CONNECT_TIMEOUT = 5000;

    /**
     * socket超时时间
     */
    private static int DEFAULT_SOCKET_TIMEOUT = 3000;

    public static CloseableHttpClient createSSLClient(RequestConfig config) {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
                    null, new TrustStrategy() {
                        // 信任所有
                        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            return true;
                        }

                    }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            return HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(config).build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    public static CloseableHttpClient createDefaultSSLClient() {
        return createSSLClient(assmebleDefaultRequestConfig());
    }

    public static CloseableHttpClient createDefaultHttpClient() {
        return createHttpClient(assmebleDefaultRequestConfig());
    }

    public static CloseableHttpClient createHttpClient(RequestConfig config) {
        return HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    private static RequestConfig assmebleDefaultRequestConfig() {
        return RequestConfig.custom()
                    .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                    .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();

    }

    public static ResponseResult executeHttpPost(HttpEntity httpEntity, String httpUrl) throws IOException {
        return executeHttpPost(defaultHttpClient,httpEntity,httpUrl);
    }

    public static ResponseResult executeHttpPost(CloseableHttpClient httpClient,HttpEntity httpEntity, String httpUrl) throws IOException {
        if(httpClient == null || httpEntity == null || StringUtils.isEmpty(httpUrl)){
            return null;
        }

        ResponseResult responseResult = new ResponseResult();

        CloseableHttpResponse response = null;
        HttpEntity responseEntity = null;
        try {
            HttpPost httpPost = new HttpPost(httpUrl);
            httpPost.setEntity(httpEntity);

            response = httpClient.execute(httpPost);
            responseEntity = response.getEntity();

            responseResult.setHeaders(response.getAllHeaders());
            responseResult.setLocale(response.getLocale());
            responseResult.setProtocolVersion(response.getProtocolVersion());
            responseResult.setStatusLine(response.getStatusLine());
            byte[] content = EntityUtils.toByteArray(responseEntity);
            responseResult.setByteContent(content);
            responseResult.setStringContent(new String(content, "utf-8"));

            return responseResult;
        }
        catch (IOException e) {
            throw e;
        } finally {
            try {
                EntityUtils.consume(responseEntity);
            } catch (IOException e) {
                log.info("consume error" + responseEntity);
            }
        }
    }

    public static ResponseResult executeHttpGet(String httpUrl) throws IOException{
        return executeHttpGet(defaultHttpClient,httpUrl);
    }

    public static ResponseResult executeHttpGet(CloseableHttpClient httpClient,String httpUrl) throws IOException{
        if(httpClient == null || StringUtils.isEmpty(httpUrl)){
            return null;
        }

        ResponseResult responseResult = new ResponseResult();

        CloseableHttpResponse response = null;
        HttpEntity responseEntity = null;
        try {
            HttpGet httpGet = new HttpGet(httpUrl);
            response = httpClient.execute(httpGet);
            responseEntity = response.getEntity();

            responseResult.setHeaders(response.getAllHeaders());
            responseResult.setLocale(response.getLocale());
            responseResult.setProtocolVersion(response.getProtocolVersion());
            responseResult.setStatusLine(response.getStatusLine());
            byte[] content = EntityUtils.toByteArray(responseEntity);
            responseResult.setByteContent(content);
            responseResult.setStringContent(new String(content, "utf-8"));

            return responseResult;
        }
        catch (IOException e) {
            throw e;
        } finally {
            try {
                EntityUtils.consume(responseEntity);
            } catch (IOException e) {
                log.info("consume error" + responseEntity);
            }
        }
    }

    public static ResponseResult executeHttpGet(String serverUrl, Map<String,String> param) throws IOException{
        return executeHttpGet(defaultHttpClient,serverUrl,param);
    }

    public static ResponseResult executeHttpGet(CloseableHttpClient httpClient, String serverUrl, Map<String,String> param) throws IOException{
        if(httpClient == null || StringUtils.isEmpty(serverUrl)){
            return null;
        }
        StringBuilder httpUrlBuilder = new StringBuilder(serverUrl);
        String resultHttpUrl = null;
        if(!CollectionUtils.isEmpty(param)){
            httpUrlBuilder.append("?");
            for(Map.Entry<String,String> paramEntry : param.entrySet()){
                httpUrlBuilder.append(paramEntry.getKey()).append("=").append(URLEncoder.encode(paramEntry.getValue().trim(),"utf-8")).append("&");
            }
            String httpUrl = httpUrlBuilder.toString();
            resultHttpUrl  = httpUrl.substring(0,httpUrl.length()-1);
        }
        return executeHttpGet(httpClient,resultHttpUrl);
    }

}
