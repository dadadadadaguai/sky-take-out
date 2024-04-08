package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

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
            Map<String, LocalDateTime> map = new HashMap<>();
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

    /**
     * 订单统计
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public OrderReportVO getOrderReport(LocalDate beginTime, LocalDate endTime) {
        //日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(beginTime);
        while (!beginTime.equals(endTime)) {
            beginTime = beginTime.plusDays(1);
            dateList.add(beginTime);
        }
/*      //每天订单数 select count(id) from order where orderTime<? and orderTime>?
        //每天有效订单数 select count(id) from order where order
        //有效订单总数 select count(id) from order where orderTime<?  and status==已完成
        //订单总数 select count(id) from order where orderTime<?*/
        List<Integer> localOrderNum = new ArrayList<>();
        List<Integer> userfulLocalOrderNum = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //获取当天的时间范围0-24
            LocalDateTime beginTimeLocal = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTimeLocal = LocalDateTime.of(localDate, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("endTime", endTimeLocal);
            map.put("beginTime", beginTimeLocal);
            Integer orderCount = orderMapper.getOrderNum(map);  //订单
            map.put("status", Orders.COMPLETED);
            Integer userfulOrderCount = orderMapper.getOrderNum(map); //有效订单
            userfulLocalOrderNum.add(userfulOrderCount);
            localOrderNum.add(orderCount);
        }
        Integer totalOrderCount = localOrderNum.stream().reduce(Integer::sum).get();   //订单总数
        Integer totalUserfulOrderCount = userfulLocalOrderNum.stream().reduce(Integer::sum).get(); //有效订单总数
        //订单完成率:有效订单/订单总数
        // doubleValue() 将Integer强转为Double
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = totalUserfulOrderCount.doubleValue() / totalOrderCount;
        }
        String stringDateList = StringUtils.join(dateList, ",");
        String stringLocalOrderNum = StringUtils.join(localOrderNum, ",");
        String stringUserfulLocalOrderNum = StringUtils.join(userfulLocalOrderNum, ",");
        return OrderReportVO.builder()
                .dateList(stringDateList)
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(stringLocalOrderNum)
                .totalOrderCount(totalOrderCount)
                .validOrderCountList(stringUserfulLocalOrderNum)
                .validOrderCount(totalUserfulOrderCount)
                .build();
    }

    /**
     * 销量排名top10接口
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        //sql select od.name,sum(od.number) from order o,orderDetail od where o.id=od.order_id and o.status=6 and ordertime<? and ordertime>?
        // group by od.name order by desc limit 0,1
        //商品名称列表
        //销量列表

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesList = orderMapper.getSalesTop(beginTime, endTime);
        //使用steam流获取nameList列表

        List<String> nameList = goodsSalesList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String stringNameList = StringUtils.join(nameList, ",");
        List<Integer> numberList = goodsSalesList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String stringNumberList = StringUtils.join(numberList, ",");

        return SalesTop10ReportVO.builder()
                .nameList(stringNameList)
                .numberList(stringNumberList)
                .build();
    }

    /**
     * 导出报表 30天
     *
     * @param response
     */
    @Override
    public void exportExcel(HttpServletResponse response) {
        //获取数据
        LocalDate localDate = LocalDate.now();
        LocalDate dateBegin = localDate.minusDays(30);  //开始这一天
        LocalDate dateEnd = localDate.minusDays(1);    //结束

        LocalDateTime beginTime = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);

        //填充数据到excel模板
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/datetemplet.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //时间段
            XSSFSheet sheet1 = excel.getSheet("sheet1");
            sheet1.getRow(1).getCell(1).setCellValue("订单时期为:" + dateBegin + "至" + dateEnd);
            //概览数据
            sheet1.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet1.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet1.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            sheet1.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet1.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());
            //明细数据:每一天的数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO detailBusinessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                XSSFRow row = sheet1.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(detailBusinessData.getTurnover());
                row.getCell(3).setCellValue(detailBusinessData.getValidOrderCount());
                row.getCell(4).setCellValue(detailBusinessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(detailBusinessData.getUnitPrice());
                row.getCell(6).setCellValue(detailBusinessData.getNewUsers());
            }
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            outputStream.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
