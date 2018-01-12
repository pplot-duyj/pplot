package com.pp.lot.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class QiniuController {

    private static Logger logger = LoggerFactory.getLogger(QiniuController.class);

    @RequestMapping("uptoken")
    public Map uptoken(){
        String accessKey = "XjhQ4r1KFCdE_Gmn_azQfg5QhBh-BgTLJNB00UPU";
        String secretKey = "UcYabowYHeQ1Q22SeYeSgQ5ty2K87xZmTSyg4HZh";
        String bucket = "korn";
        Auth auth = Auth.create(accessKey, secretKey);
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fsize\":$(fsize)}");
        long expireSeconds = 3600;
        String upToken = auth.uploadToken(bucket, null, expireSeconds, putPolicy);
        Map result = new HashMap();
        result.put("uptoken", upToken);
        return result;
    }
}
