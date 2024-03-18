package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.controller.admin.SetmealController;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    @Autowired
    private DishMapper dishMapper;


    /**
     * 添加套餐
     *
     * @param setmealDTO
     */
    @Override
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.add(setmeal);

        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //新增套餐时添加了菜品
        if (setmealDishes != null || setmealDishes.size() != 0) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealId);
                setmealDish.setDishId(setmealDish.getDishId());
            }
            setMealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());  //自动分页
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        long total = page.getTotal();
        List<SetmealVO> result = page.getResult();
        return new PageResult(total, result);
    }

    /**
     * 查询套餐信息
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO querySetmeal(long id) {
        Setmeal setmeal = setmealMapper.queryAll(id);
        List<SetmealDish> setmealDishes = setMealDishMapper.getSetMealIdByDishId(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     */

    @Override
    public void updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        Long setmealId = setmeal.getId();
        //更新套餐菜品表考虑先删除这表再插入表
        setMealDishMapper.deleteSetmealDish(setmealId);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        setMealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     */
    @Override
    public void deleteSetmeal(List<Long> ids) {
        //判断套餐下有无起售商品，有则无法删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.queryAll(id);
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        for (Long setmealId : ids) {
            setmealMapper.delete(setmealId);
            //删除关联的套餐菜品
            setMealDishMapper.deleteSetmealDish(setmealId);
        }
    }

    /**
     * 禁用或者启用套餐
     *
     * @param status
     * @param id
     */
    @Override
    public void openOrForbid(Integer status, Long id) {
        //当启用套餐时，要判断套餐的商品是否启用，禁用则抛出异常
        if (status.equals(StatusConstant.ENABLE)) {
            List<Dish> dishList = dishMapper.getBySetMealId(id);
            if (dishList != null && dishList.size() > 0) {
                for (Dish dish : dishList) {
                    if (dish.getStatus().equals(StatusConstant.DISABLE)){
                        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
                    }
                }
            }
        }
        Setmeal setmeal = Setmeal.builder().status(status).id(id).build();
        setmealMapper.update(setmeal);
    }
}
