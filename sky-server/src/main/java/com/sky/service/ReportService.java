package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
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

    /**
     * 销量排名统计
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    /**
     * 导出报表
     * @param response
     */
    void exportExcel(HttpServletResponse response);
}
