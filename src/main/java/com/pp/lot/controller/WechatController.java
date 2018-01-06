package com.pp.lot.controller;

import com.pp.lot.service.GeneralService;
import com.pp.lot.util.WechatJsSDKUtil;
import com.pp.lot.util.WechatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("wechat")
@RestController
public class WechatController {

    private static Logger logger = LoggerFactory.getLogger(WechatController.class);

    @Autowired
    private GeneralService generalService;

    @Autowired
    private WechatUtil wechatUtil;
    @Autowired
    private WechatJsSDKUtil wechatJsSDKUtil;

    @RequestMapping(value = "gateway", produces = "text/xml;charset=utf-8")
    public String get(HttpServletRequest request, HttpServletResponse response) {
        return wechatUtil.processRequest(request);
    }

    @RequestMapping("getAccessToken")
    public Map getAccessToken(){
        Map map = new HashMap();
        map.put("result", wechatUtil.getAccessToken());
        return map;
    }

    @RequestMapping("getJSSDKParam")
    public Map getJSSDKParam(String currUrl){
        Map map = new HashMap();
        map.put("result", wechatJsSDKUtil.getJSSDKParam(currUrl));
        return map;
    }

}
