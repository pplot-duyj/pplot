package com.pp.lot.service;

import com.pp.lot.controller.GeneralController;
import com.pp.lot.mapper.GeneralMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeneralService {

    private static Logger logger = LoggerFactory.getLogger(GeneralController.class);

    @Autowired
    private GeneralMapper generalMapper;

    public Map queryData(){
        logger.info("GeneralService");
        List<Map> list = generalMapper.queryData();
        Map result = new HashMap();
        result.put("data", list);
        return result;
    }
}
