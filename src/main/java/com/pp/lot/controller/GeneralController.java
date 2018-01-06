package com.pp.lot.controller;

import com.pp.lot.service.GeneralService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GeneralController {

    //private static Log logger = LogFactory.getLog(GeneralController.class);
    private static Logger logger = LoggerFactory.getLogger(GeneralController.class);

    @Autowired
    private GeneralService generalService;

    @RequestMapping("test")
    public Map test(){
        logger.info("GeneralController");
        return generalService.queryData();
    }
}
