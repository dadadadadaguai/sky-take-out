package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

    @Select("select count(id) from dish where id=category_id")
    Integer countByCategoryId(Long categoryId);

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<Dish> query(DishPageQueryDTO dishPageQueryDTO);
}
