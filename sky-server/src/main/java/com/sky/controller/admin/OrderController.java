package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminOrderController")
@Slf4j
@RequestMapping(" /admin/order")
@Api(tags = "订单管理")
public class OrderController {
        @Autowired
        private OrderService orderService;

        @GetMapping("/conditionSearch")
        @ApiOperation("订单分页查询")
        public Result<PageResult> PageQueryOrder(OrdersPageQueryDTO ordersPageQueryDTO){
                log.info("订单分页查询,传入数据为:{}",ordersPageQueryDTO);
                PageResult result=orderService.pageQueryOrder(ordersPageQueryDTO);
                return Result.success(result);
        }
        @GetMapping("/statistics")
        public Result<OrderStatisticsVO> statistics(){
                OrderStatisticsVO orderStatisticsVO=orderService.statistics();
                return Result.success(orderStatisticsVO);
        }

}
