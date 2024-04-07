package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Override
    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        //日期
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        String stringDateList = StringUtils.join(dateList, ",");
        //营业额 select count(amount) from order where orderTime>beginTime and orderTime<EndTime and status==?

        List<Double> turnoverList=new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime EndTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("beginTime",beginTime);
            map.put("EndTime",EndTime);
            map.put("status", Orders.COMPLETED);
            Double turnover =orderMapper.sumByMap(map);
            //避免turnover为null
            turnover= turnover==null?0.0:turnover;
            turnoverList.add(turnover);

        }

        String StringturnoverList = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO
                .builder()
                .dateList(stringDateList)
                .turnoverList(StringturnoverList)
                .build();
    }
}
