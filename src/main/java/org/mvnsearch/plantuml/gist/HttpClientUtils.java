package org.mvnsearch.plantuml.gist;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.IOException;

/**
 * http client utils
 *
 * @author linux_china
 */
public class HttpClientUtils {
    private static HttpClient httpClient;

    public static String getResponseBody(String httpUrl) throws IOException {
        GetMethod getMethod = new GetMethod(httpUrl);
        getHttpClient().executeMethod(getMethod);
        return getMethod.getResponseBodyAsString();
    }

    public static String getResponseBody(String userName, String password, String httpUrl) throws IOException {
        GetMethod getMethod = new GetMethod(httpUrl);
        String encoding = Base64.encodeBase64String((userName + ":" + password).getBytes());
        getMethod.addRequestHeader("Authorization", "Basic " + encoding);
        getHttpClient().executeMethod(getMethod);
        return getMethod.getResponseBodyAsString();
    }

    public static HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = createHttpClient();
        }
        return httpClient;
    }

    /**
     * create http client instance
     *
     * @return HttpClient object
     */
    private static HttpClient createHttpClient() {
        HttpClient clientTemp = new HttpClient();     //HttpClient create
        HttpClientParams clientParams = clientTemp.getParams();
        clientParams.setParameter("http.useragent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0");
        clientParams.setParameter("http.socket.timeout", 10000); //10 seconds for socket waiting
        clientParams.setParameter("http.connection.timeout", 10000); //10 seconds http connection creation
        clientParams.setParameter("http.connection-manager.timeout", 3000L); //3 seconds waiting to get connection from http connection manager
        clientParams.setParameter("http.method.retry-handler", new DefaultHttpMethodRetryHandler()); // if failed, try 3
        return clientTemp;
    }
}
