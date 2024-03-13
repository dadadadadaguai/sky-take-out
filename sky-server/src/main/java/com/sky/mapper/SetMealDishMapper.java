package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
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
}
