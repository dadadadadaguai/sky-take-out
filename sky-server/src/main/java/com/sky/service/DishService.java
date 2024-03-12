package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;


public interface DishService {

    public PageResult PageQuery(DishPageQueryDTO dishPageQueryDTO);

    void insertDish(DishDTO dishDTO);

    void deleteDish(List<Long> ids);

    void updateDish(DishDTO dishDTO);

    DishVO selectById(Long id);

    List<Dish> selectByCategoryId(Long categoryId);
}
