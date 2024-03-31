package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 查询购物车
     *
     * @param shoppingCart
     * @return
     */

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据Id修改商品数量
     *
     * @param shoppingCart
     */

    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void update(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     *
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "values (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 清空购物车
     *
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long userId);

    /**
     * 删除菜品
     *
     * @param shoppingCart
     */
    @Delete("delete from shopping_cart where user_id=#{userId} and dish_id=#{dishId}")
    void deleteDish(ShoppingCart shoppingCart);

    /**
     * 删除套餐
     *
     * @param shoppingCart
     */
    @Delete("delete from shopping_cart where user_id=#{userId} and setmeal_id=#{setmealId}")
    void deleteSetmealId(ShoppingCart shoppingCart);
}
