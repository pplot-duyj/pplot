package com.pp.lot.service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * 版权所有(C) 2017 上海银路投资管理有限公司
 * 描述:
 * 作者: liufurong
 * 创建日期: 2017-02-24
 * 修改记录:
 */
public class HttpClient {
    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    private static PoolingHttpClientConnectionManager connMgrPool;

    private static void init() {
        if (connMgrPool == null) {
            connMgrPool = new PoolingHttpClientConnectionManager();
            connMgrPool.setMaxTotal(200);// 连接池最大连接数
            connMgrPool.setDefaultMaxPerRoute(50);// 每个路由最大连接数，默认是2
        }
    }

    /**
     * 通过连接池获取HTTPclient
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        init();
        return HttpClients.custom().setConnectionManager(connMgrPool).build();
    }

    /**
     *参数为文件的请求
     *@param name 文件的键值
     *@param file
     * @param filename 文件名称
     * @param param 请求参数，可为空
     * */
    public static String multipartHttpPost(String url, MultipartFile file, String filename, String name, Map<String,Object> param) throws IOException{

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        //将文件转为流添加到entityBuider中
        builder.addBinaryBody(name, file.getInputStream(), ContentType.MULTIPART_FORM_DATA,filename);//
        builder.setCharset(Charset.forName("UTF-8"));

        //添加请求参数
        if(!StringUtil.isEmpty(param)){
            for(Map.Entry<String,Object> entry:param.entrySet()){

                ContentType contentType = ContentType.create("text/plain", Charset.forName("UTF-8"));
                StringBody stringBody = new StringBody((String) entry.getValue(),contentType);
                builder.addPart(entry.getKey(), stringBody);
                //builder.addTextBody(entry.getKey(), entry.getValue().toString());// 类似浏览器表单提交，对应input的name和value
            }

        }

        HttpEntity entity = builder.build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
       /* HttpResponse response = httpClient.execute(httpPost);// 执行提交
        HttpEntity responseEntity = response.getEntity();*/

        return getResponseString(getResult(httpPost));
    }


    /**
     * 此方法暂时不加载任何证书
     * @return
     * @throws IOException
     */
    private static CloseableHttpClient getHttpsClient() throws IOException {
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom()
                .loadTrustMaterial(new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                })
                .build();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            sslcontext,
            new String[] { "TLSv1" },
            null,
            new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connMgrPool)
            .setSSLSocketFactory(sslsf)
            .build();
        return httpclient;
    }

    /**
     * 处理https post请求
     * @param url
     * @param map
     * @param charset
     * @return
     */
    public static String doHttpsPost(String url, Map<String,String> map, String charset) throws IOException {
        if(StringUtil.isEmpty(charset))
            charset = "UTF-8";
        org.apache.http.client.HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        httpClient = getHttpsClient();
        httpPost = new HttpPost(url);
        //设置参数
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        Iterator iterator = map.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,String> elem = (Map.Entry<String, String>) iterator.next();
            list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
        }
        if(list.size() > 0){
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
            httpPost.setEntity(entity);
        }
        HttpResponse response = httpClient.execute(httpPost);
        if(response != null){
            HttpEntity resEntity = response.getEntity();
            if(resEntity != null){
                result = EntityUtils.toString(resEntity,charset);
            }
        }
        return result;
    }

    public static String doHttpsPostRowParam(String url, String param, String charset) throws IOException {
        if(StringUtil.isEmpty(charset))
            charset = "UTF-8";
        org.apache.http.client.HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        httpClient = getHttpsClient();
        httpPost = new HttpPost(url);
        //设置参数
        EntityBuilder entity = EntityBuilder.create();
        entity.setText(param);
        httpPost.setEntity(entity.build());
        HttpResponse response = httpClient.execute(httpPost);
        if(response != null){
            HttpEntity resEntity = response.getEntity();
            if(resEntity != null){
                result = EntityUtils.toString(resEntity,charset);
            }
        }
        return result;
    }

    /**
     * Http GET请求，不带任何参数
     * @param url
     * @return
     */
    public static String doHttpGet(String url) throws IOException {
        HttpGet httpGet =  new HttpGet(url);
        return getResponseString(getResult(httpGet));
    }

    /**
     * Http GET请求，携带请求参数
     * @param url
     * @param params
     * @return
     * @throws URISyntaxException
     */
    public static String doHttpGet(String url,Map<String, Object> params) throws URISyntaxException, IOException {
        URIBuilder uri = new URIBuilder();
        uri.setPath(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        uri.setParameters(pairs);
        HttpGet httpGet = new HttpGet(uri.build());
        return  getResponseString(getResult(httpGet));
    }

    /**
     * Http GET请求，携带头信息和参数
     * @param url
     * @param headers
     * @param params
     * @return
     * @throws URISyntaxException
     */
    public static String doHttpGet(String url, Map<String, Object> headers, Map<String, Object> params) throws URISyntaxException, IOException {
        URIBuilder uri = new URIBuilder();
        uri.setPath(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        uri.setParameters(pairs);
        HttpGet httpGet = new HttpGet(uri.build());
        for(Map.Entry<String, Object> param : headers.entrySet()){
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return  getResponseString(getResult(httpGet));
    }

    /**
     * Http POST请求，不携带消息体
     * @param url
     * @return
     */
    public static String doHttpPost(String url) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        return  getResponseString(getResult(httpPost));
    }

    /**
     * Http POST请求，字符串参数
     * @param url
     * @param str
     * @return
     */
    public static String doHttpPost(String url, String str) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setHeader("Accept","*");
        StringEntity stringEntity = new StringEntity(str, "UTF-8");
        stringEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse response = getResult(httpPost);
        String result = getResponseString(response);
        return result;
    }

    /**
     * Http POST请求，字符串参数
     * @param url
     * @return
     */
    public static File doHttpgetAndRetureStream(String url) throws Exception {

        HttpGet httpGet =  new HttpGet(url);
        CloseableHttpClient httpClient = getHttpClient();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000).setConnectionRequestTimeout(150000).setSocketTimeout(200000).build();
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = httpClient.execute(httpGet);

        InputStream in = response.getEntity().getContent();
        File file = new File("../report");
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buf = new byte[8096];
        int size = 0;
        while ((size = in.read(buf)) != -1)
            fos.write(buf, 0, size);
        fos.close();
        return file;
    }

    public static String doHttpJson(String url, String str) throws IOException {
        String result = "";
        String logMsg;
        try {
            HttpPost httpPost = new HttpPost(url);
            RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).build();

            httpPost.setConfig(config);
            StringEntity stringEntity = new StringEntity(str, "UTF-8");
            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            CloseableHttpResponse response = getResult(httpPost);
            response.setHeader("Content-Type", "application/json; charset=utf-8");


            result = getResponseString(response);
            logMsg = result;
        } catch (Exception e) {
            logMsg = e.getMessage();
        }
        log.info("=======http request======= url:{} - data:{} - res:{}\n", url, str, logMsg);

        return result;
    }

    public static String doHttpJson(String url, String str, int timeout) {
        String result = "";
        String logMsg;
        try {
            HttpPost httpPost = new HttpPost(url);
            RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();

            httpPost.setConfig(config);
            StringEntity stringEntity = new StringEntity(str, "UTF-8");
            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            CloseableHttpClient httpClient = getHttpClient();
            CloseableHttpResponse response = httpClient.execute(httpPost);
            response.setHeader("Content-Type", "application/json; charset=utf-8");

            result = getResponseString(response);
            logMsg = result;
        } catch (Exception e) {
            logMsg = e.getMessage();
        }
        log.info("=======http request======= url:{} - data:{} - res:{}\n", url, str, logMsg);

        return result;
    }

    /**
     * 获取返回字符串
     * @param response
     * @return
     * @throws IOException
     */
    public static String getResponseString(CloseableHttpResponse response) throws IOException {
        String result = null;
        try {
            // 得到返回对象
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // 获取返回结果
                result = EntityUtils.toString(entity,"utf-8");
            }
        } catch (IOException e) {
            log.error("IOException", e);
            throw e;
        } finally {
            // 关闭到客户端的连接
            try {
                if(response != null){
                    response.close();
                }
            } catch (IOException e) {
                log.error("IOException", e);
                throw e;
            }
        }
        return result;
    }


    /**
     * Http POST请求，携带请求消息体
     * @param url
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String doHttpPost(String url,Map<String, Object> params) throws IOException {
        String res = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> pairs = null;
            if (params != null && !params.isEmpty()) {
                pairs = new ArrayList<NameValuePair>(params.size());
                for (String key : params.keySet()) {
                    if (params.get(key) != null)
                        pairs.add(new BasicNameValuePair(key, params.get(key).toString()));
                }
            }

            if (pairs != null && pairs.size() > 0) {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
            }
            res = getResponseString(getResult(httpPost));
        } catch (IOException e) {
            res = e.toString();
            throw e;
        } finally {
            log.info("=======http request======= url:{} - data:{} - res:{}\n", url, params, res);
        }

        return res;
    }

    /**
     * Http POST请求，携带请求消息体,请求头参数
     * @param url
     * @param params
     * @param headerMap
     * @return
     * @throws UnsupportedEncodingException
     */
    public static CloseableHttpResponse httpPostRequest(String url,Map<String, Object> params,Map<String,String> headerMap) throws IOException {
        HttpPost httpPost = new HttpPost(url);

        //添加请求头
        if(headerMap !=null && headerMap.entrySet().size()>0){
            for(Map.Entry<String, String> entry : headerMap.entrySet()){
                httpPost.addHeader(entry.getKey(),entry.getValue());
            }
        }
        String jsonValue = covertParams2JSON(params);
        StringEntity stringEntity = new StringEntity(jsonValue, "UTF-8");
        httpPost.setEntity(stringEntity);
        return getResult(httpPost);
    }
    /**
     * Http POST请求，携带请求消息体,请求头参数
     * @param url
     * @param jsonValue
     * @param headerMap
     * @return
     * @throws UnsupportedEncodingException
     */
    public static CloseableHttpResponse httpPostRequestForJson(String url,String jsonValue,Map<String,String> headerMap) throws IOException {
        HttpPost httpPost = new HttpPost(url);

        //添加请求头
        if(headerMap !=null && headerMap.entrySet().size()>0){
            for(Map.Entry<String, String> entry : headerMap.entrySet()){
                httpPost.addHeader(entry.getKey(),entry.getValue());
            }
        }

        StringEntity stringEntity = new StringEntity(jsonValue, "UTF-8");
        httpPost.setEntity(stringEntity);
        return getResult(httpPost);
    }
    /**
     * Http POST请求，携带请求消息体
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String gethttpPostRequest(String url,Map<String, Object> params) throws IOException {
        String json  = covertParams2JSON(params);
        HttpPost post = new HttpPost(url);
        StringEntity postingString = new StringEntity(json);// json传递
        postingString.setContentEncoding("UTF-8");
        post.setEntity(postingString);
        post.setHeader("ContentEncoding", "UTF-8");
        post.setHeader("Content-Type", "application/json; charset=utf-8");
        CloseableHttpResponse response = getResult(post);
        String content = getResponseString(response);
        //EntityUtils.toString(response.getEntity());

        return content;

    }

    /**
     * post发送文件和参数
     * @param url
     * @param param
     * @param file
     * */
  /*  public static String postFileAndParam(String url,Map<String,Object> param,File file){

    }*/

    /**
     * Http POST请求，携带消息头和请求消息体
     * @param url
     * @param contentType
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static CloseableHttpResponse httpPostRequest(String url,String contentType,Map<String, Object> params) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        String jsonValue = covertParams2JSON(params);
        StringEntity stringEntity = new StringEntity(jsonValue, "UTF-8");
        stringEntity.setContentType(contentType);
        httpPost.setEntity(stringEntity);
        return getResult(httpPost);
    }

    public static void download(String url,String json, String filepath) {
        try {
            CloseableHttpClient client = getHttpClient();
            HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(json, "UTF-8");
            httpPost.setHeader("ContentEncoding", "UTF-8");
            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            httpPost.setEntity(stringEntity);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();
            httpPost.setConfig(requestConfig);
            CloseableHttpResponse response = client.execute(httpPost);

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            if (filepath == null)
                filepath = getFileName(response);
            File file = new File(filepath);
            file.getParentFile().mkdirs();
            FileOutputStream fileout = new FileOutputStream(file);
            /**
             * 根据实际运行效果 设置缓冲区大小
             */
            byte[] buffer=new byte[10 * 1024];
            int ch = 0;
            while ((ch = is.read(buffer)) != -1) {
                fileout.write(buffer,0,ch);
            }
            is.close();
            fileout.flush();
            fileout.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出文件
     * @param file
     * @param response
     * @throws IOException
     */
    public static void exportReportFile(File file, HttpServletResponse response, String fileName) throws IOException{
        RandomAccessFile raf = null;
        OutputStream responseOS = response.getOutputStream();
        try {
            responseOS = response.getOutputStream();
            response.addHeader("Content-Length", "" + file.length());
            response.setHeader("content-disposition",
                "attachment;filename=" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));
            raf = new RandomAccessFile(file, "rw");
            byte[] buffer = new byte[1024 * 1024];
            int avariable = -1;
            while ((avariable = raf.read(buffer)) > 0) {
                responseOS.write(buffer, 0, avariable);
            }
            buffer = null;
            responseOS.flush();
        } catch (IOException e) {
            throw e;
        }  finally {
            raf.close();
            file.delete();
            responseOS.close();
        }
    }

    private static String getFileName(CloseableHttpResponse response) {
        Header contentHeader = response.getFirstHeader("Content-Disposition");
        String filename = null;
        if (contentHeader != null) {
            HeaderElement[] values = contentHeader.getElements();
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename");
                if (param != null) {
                    try {
                        filename = new String(param.getValue().toString().getBytes(), "utf-8");
//                        filename=URLDecoder.decode(param.getValue(),"utf-8");
//                        filename = param.getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return filename;
    }


    /**
     * 执行HTTP请求
     * @param request
     * @return
     */
    private static CloseableHttpResponse getResult(HttpRequestBase request) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        /**
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(15000).setSocketTimeout(20000).build();
        request.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            log.info("HttpResponse code:{}", statusCode);
            return response;
        }catch (IOException e) {
            log.error("IOException:", e);
            throw e;
        }

    }

    /**
     * 转换请求参数
     * @param params
     * @return
     */
    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params){
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for(Map.Entry<String, Object> param : params.entrySet()){
            pairs.add(new BasicNameValuePair(param.getKey(), (String) param.getValue()));
        }
        return pairs;
    }

    /**
     * 转换请求参数
     * @param params
     * @return
     */
    private static String covertParams2JSON(Map<String, Object> params){
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(params);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
