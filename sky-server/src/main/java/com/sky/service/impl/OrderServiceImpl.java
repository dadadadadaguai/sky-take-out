package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.task.WebSocketServer;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j

public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;


    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //1. 处理各种业务异常（地址簿为空、购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //检查用户的收货地址是否超出配送范围
        //checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        //查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            //抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //2. 向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setAddress(addressBook.getDetail());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //3. 向订单明细表插入n条数据
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();//订单明细
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置当前订单明细关联的订单id
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //4. 清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        //5. 封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
/*        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付

        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新

        LocalDateTime check_out_time = LocalDateTime.now();
        Long orderid=orderMapper.selectId(ordersPaymentDTO.getOrderNumber());
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderid);*/
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        //通过websocket向客户端推送消息
        Map map = new HashMap<>();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:"+outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

    }

    /**
     * 订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQueryOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVoList = getOrderVoList(page);
        return new PageResult(page.getTotal(), orderVoList);
    }

    private List<OrderVO> getOrderVoList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                //封装orderDishs
                String orderDishesStr = getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishesStr);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        //对每条订单进行函数处理，
        List<String> ordishList = orderDetailList.stream().map(orderDetail -> {
            String orderDish = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());
        return String.join("", ordishList);   //将字符串列表的字符串连接
    }

    /**
     * 用户端历史订单查询
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult page(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        Long userId = BaseContext.getCurrentId();
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(userId);
        ordersPageQueryDTO.setStatus(status);
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);

        //查询菜品详情
        List<OrderVO> list = new ArrayList<>();
        if (orders != null && orders.getTotal() > 0) {
            for (Orders order : orders) {
                //每一个订单的订单详情
                Long orderId = order.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }
        return new PageResult(orders.getTotal(), list);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO queryOrder(Long id) {
        Orders orders = orderMapper.selectAllById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        Orders orderDb = orderMapper.selectAllById(id);
        //输入性检查：判断有无订单
        if (orderDb == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //当订单状态为：3已接单 4派送中 5已完成 6已取消,则无法取消订单
        Integer orderDbStatus = orderDb.getStatus();
        if (orderDbStatus > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        //当订单状态为待接单时取消订单，则进行退款
        if (orderDbStatus == 2) {
            orders.setPayMethod(Orders.REFUND);
        }
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orders.setId(id);
        orderMapper.update(orders);
    }


    /**
     * 再来一单
     * 将该订单的数据重新加入到购物车
     *
     * @param id
     */
    @Override
    public void repetOrder(Long id) {
        Orders ordersDb = orderMapper.selectAllById(id);
        if (ordersDb == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        Long userId = BaseContext.getCurrentId();
        //将订单的菜品和套餐加入到购物车
        List<Object> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        //批量插入
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 管理端接单
     *
     * @param ordersConfirmDTO
     */
    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderMapper.selectAllById(ordersConfirmDTO.getId());
        //避免状态异常
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orderDb = Orders.builder()
                .status(Orders.CONFIRMED)
                .id(ordersConfirmDTO.getId())
                .build();
        orderMapper.update(orderDb);
    }

    /**
     * 管理端拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.selectAllById(ordersRejectionDTO.getId());
        //避免状态异常
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //拒单后需要进行退款
        Orders ordersDb = Orders.builder()
                .status(Orders.CANCELLED)
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.REFUND)
                .id(ordersRejectionDTO.getId())
                .build();
        orderMapper.update(ordersDb);
    }

    /**
     * 管理端取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orderDb = orderMapper.selectAllById(ordersCancelDTO.getId());
        Integer payStatus = orderDb.getPayStatus();
        Orders orders = Orders.builder()
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .id(ordersCancelDTO.getId())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();
        //处理不同的支付状态
        if (Objects.equals(payStatus, Orders.PAID)) {
            orders.setPayStatus(Orders.REFUND);//已支付时
        }
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void deliveryOrder(Long id) {
        Orders orderDb = orderMapper.selectAllById(id);
        if (!orderDb.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改订单状态
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void completeOrder(Long id) {
        Orders orderDb = orderMapper.selectAllById(id);
        if (orderDb==null || !orderDb.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .status(Orders.COMPLETED)
                .id(id)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 客户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders ordersDB = orderMapper.selectAllById(id);

        if (ordersDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map = new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号:"+ordersDB.getNumber());

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }
}
