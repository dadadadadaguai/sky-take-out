package com.sky.service;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    void addSetmeal(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据ID查询套餐信息
     * @param id
     * @return
     */
    SetmealVO querySetmeal(long id);

    /**
     * 更新套餐信息
     * @param setmealDTO
     */
    @AutoFill(OperationType.UPDATE)
    void updateSetmeal(SetmealDTO setmealDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteSetmeal(List<Long> ids);

    /**
     * 禁用或者启用
     * @param status
     * @param id
     */
    void openOrForbid(Integer status, Long id);
}
