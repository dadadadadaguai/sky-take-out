package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@Slf4j
@RequestMapping("/admin/order")
@Api(tags = "订单管理")
public class OrderController {
        @Autowired
        private OrderService orderService;

        /**
         * 订单分页查询
         * @param ordersPageQueryDTO
         * @return
         */
        @GetMapping("/conditionSearch")
        @ApiOperation("订单分页查询")
        public Result<PageResult> PageQueryOrder(OrdersPageQueryDTO ordersPageQueryDTO){
                log.info("订单分页查询,传入数据为:{}",ordersPageQueryDTO);
                PageResult result=orderService.pageQueryOrder(ordersPageQueryDTO);
                return Result.success(result);
        }

        /**
         * 各个状态的订单数量统计
         * @return
         */
        @GetMapping("/statistics")
        @ApiOperation("各个状态的订单数量统计")
        public Result<OrderStatisticsVO> statistics(){
                log.info("各个状态的订单数量统计");
                OrderStatisticsVO orderStatisticsVO=orderService.statistics();
                return Result.success(orderStatisticsVO);
        }

        /**
         * 查询订单详情
         * @param id
         * @return
         */
        @GetMapping("/details/{id}")
        @ApiOperation("查询订单详情")
        public Result<OrderVO> selectOrderDetail(@PathVariable Long id){
                log.info("查询订单详情,查询商品id:{}",id);
                OrderVO orderVO=orderService.queryOrder(id);
                return Result.success(orderVO);
        }

}
