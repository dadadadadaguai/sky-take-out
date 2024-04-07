package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     * @param beginTime
     * @param endTime
     * @return
     */
    UserReportVO getUserReport(LocalDate beginTime, LocalDate endTime);

    /**
     * 获取订单统计接口
     * @param beginTime
     * @param endTime
     * @return
     */
    OrderReportVO getOrderReport(LocalDate beginTime, LocalDate endTime);
}
