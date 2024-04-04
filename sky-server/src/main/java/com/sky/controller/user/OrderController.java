package com.sky.controller.user;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.DishVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;

@RestController("userOrderController")
@Slf4j
@RequestMapping("/user/order")
@Api(tags = "订单关联功能")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("提交订单")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {

        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单查询
     *
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("用户端历史订单查询")
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        log.info("历史订单查询,page:{},pageSize:{},status:{}", page, pageSize, status);
        PageResult result = orderService.page(page, pageSize, status);
        return Result.success(result);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result queryOrder(@PathVariable Long id) {
        log.info("查询订单:{}的详情信息", id);
        OrderVO orderVO = orderService.queryOrder(id);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     *
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        log.info("订单取消,取消的id:{}", id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetOrder(@PathVariable Long id) {
        log.info("再来一单,订单id为:{}", id);
        orderService.repetOrder(id);
        return Result.success();
    }
}
