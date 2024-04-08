package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 查询订单id
     *
     * @param orderNumber
     * @return
     */
    @Select("select id from orders where number=#{orderNumber}")
    Long selectId(String orderNumber);

    /**
     * 用于替换微信支付更新数据库状态的问题
     *
     * @param orderStatus
     * @param orderPaidStatus
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单全部数据
     * @param id
     * @return
     */
    @Select("select * from orders where id=#{id}")
    Orders selectAllById(Long id);

    /**
     * 统计不同订单状态下的数目
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status=#{status} ")
    Integer countStatus(Integer status);

    /**
     * 超时订单处理
     * @param status
     * @param orderTime
     */
    @Select("select * from orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> selectAllByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 派送中订单超时未处理
     * @param deliveryInProgress
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer deliveryInProgress, LocalDateTime orderTime);

    /**
     * 营业额统计
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 获取一段时期的订单数
     * @param map
     * @return
     */
    Integer getOrderNum(Map<String, Object> map);

    /**
     * 销量排名top10
     * @return
     */
    List<GoodsSalesDTO> getSalesTop(LocalDateTime beginTime, LocalDateTime endTime);

}
