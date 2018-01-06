package com.pp.lot.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pp.lot.service.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class WechatJsSDKUtil {

    private static Logger logger = LoggerFactory.getLogger(WechatJsSDKUtil.class);

    /**
     * 调用微信JS接口的临时票据
     *
     * @param access_token 接口访问凭证
     * @return
     */
    public static String getJsApiTicket(String access_token) {

        //todo 待缓存
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
        String requestUrl = url.replace("ACCESS_TOKEN", access_token);
        String ticket = null;
        try {
            JSONObject jsonObject = JSON.parseObject(HttpClient.doHttpGet(requestUrl));
            if (null != jsonObject) {
                ticket = jsonObject.getString("ticket");
            }
            logger.info("getJsApiTicket{}", ticket);
        } catch (Exception e) {
            logger.error("获取getJsApiTicket失败{}", e);
        }
        return ticket;
    }


    public static String getJSSDKParam(String currUrl){
        String jsapi_ticket = getJsApiTicket(WechatUtil.getAccessToken());
        Map<String, String> params = sign(jsapi_ticket, currUrl);
        JSONObject jsonObject = JSON.parseObject(JacksonUtils.mapToJson(params));
        String jsonStr = jsonObject.toString();
        return jsonStr;
    }

    public static Map<String, String> sign(String jsapi_ticket, String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String str;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        str = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;

        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    //获取当前系统时间 用来判断access_token是否过期
    public static String getTime(){
        Date dt=new Date();
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(dt);
    }
}
