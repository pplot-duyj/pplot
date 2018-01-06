package com.pp.lot.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface GeneralMapper {

    List<Map> queryData();

}
