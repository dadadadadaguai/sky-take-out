package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
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
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        //日期
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String stringDateList = StringUtils.join(dateList, ",");
        //营业额 select count(amount) from order where orderTime>beginTime and orderTime<EndTime and status==?

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime EndTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("EndTime", EndTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            //避免turnover为null
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);

        }

        String StringturnoverList = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO
                .builder()
                .dateList(stringDateList)
                .turnoverList(StringturnoverList)
                .build();
    }

    /**
     * 用户统计
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public UserReportVO getUserReport(LocalDate beginTime, LocalDate endTime) {
        //日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(beginTime);
        while (!beginTime.equals(endTime)) {
            beginTime = beginTime.plusDays(1);
            dateList.add(beginTime);
        }
        String stringDateList = StringUtils.join(dateList, ",");

        //每天的新增用户数（select count(id) from user where create_time>? and create_time<?)
        // 列表及总用户量 (select count(id) from user where create<endTime)
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> allUserList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //获取当天的时间范围0-24
            LocalDateTime beginTimeLocal = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTimeLocal = LocalDateTime.of(localDate, LocalTime.MAX);
            Map<String,LocalDateTime> map = new HashMap<>();
            map.put("endTime", endTimeLocal);
            Integer allUserNum = userMapper.getUserNum(map);
            allUserNum = allUserNum == null ? 0 : allUserNum;
            newUserList.add(allUserNum);

            map.put("beginTime", beginTimeLocal);
            Integer newUserNum = userMapper.getUserNum(map);
            newUserNum = newUserNum == null ? 0 : newUserNum;
            allUserList.add(newUserNum);
        }
        String stringNewUserList = StringUtils.join(newUserList, ",");
        String stringallUserList = StringUtils.join(allUserList, ",");

        return UserReportVO
                .builder()
                .dateList(stringDateList)
                .newUserList(stringNewUserList)
                .totalUserList(stringallUserList)
                .build();
    }
}
