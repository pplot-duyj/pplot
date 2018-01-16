package com.pp.lot.service;

import com.pp.lot.controller.ParkController;
import com.pp.lot.mapper.ParkMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParkService {

    private static Logger logger = LoggerFactory.getLogger(ParkController.class);

    @Autowired
    private ParkMapper parkMapper;

    public Map queryPark(){
        logger.info("ParkService");
        List<Map> list = parkMapper.queryPark();
        Map result = new HashMap();
        result.put("data", list);
        return result;
    }
}
