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

    void openOrForbid(Integer status, Long id);
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
