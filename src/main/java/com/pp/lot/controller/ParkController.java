package com.pp.lot.controller;

import com.pp.lot.service.ParkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ParkController {

    //private static Log logger = LogFactory.getLog(GeneralController.class);
    private static Logger logger = LoggerFactory.getLogger(ParkController.class);

    @Autowired
    private ParkService parkService;

    @RequestMapping("park")
    public Map test(){
        logger.info("ParkController");
        return parkService.queryPark();
    }
}
