package com.sky.mapper;

import com.sky.dto.DishDTO;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    void insertBatch(List<DishFlavor> flavors);

    //根据菜品ID删除口味
    @Delete("delete from dish_flavor where dish_id=#{dishId}")
    void delete(Long dishId);
    //根据菜品ID查询口味
    List<DishFlavor> selectAllByDishId(Long dishId);



    void deleteByDishId(Long dishId);
}
