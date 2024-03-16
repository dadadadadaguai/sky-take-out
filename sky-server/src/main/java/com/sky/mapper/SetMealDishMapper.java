package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {
    List<Long> getSetMealIdByDishIds(List<Long> dishIds);

    /**
     * 更新操作
     * @param setMeal
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setMeal);

    /**
     * 批量插入
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

}
