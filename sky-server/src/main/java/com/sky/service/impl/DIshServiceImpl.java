package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DIshServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult PageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.query(dishPageQueryDTO);
        long total = page.getTotal();
        List<DishVO> result = page.getResult();
        return new PageResult(total, result);
    }

    /**
     * 新增菜品
     *
     * @param dishDTO
     */
    @Override
    public void insertDish(DishDTO dishDTO) {
        log.info("新增菜品,dishDTP:{}", dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        //菜品口味处理
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null || flavors.size() != 0) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void deleteDish(List<Long> ids) {
        log.info("删除菜品,id:{}", ids);
        //1.删除当前菜品，需要考虑当前菜品的状态人，如果是在售状态，则抛出异常无法删除。
        for (Long id : ids) {
            Integer status = dishMapper.selectById(id);
            if (Objects.equals(status, StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2.删除菜品，考虑该菜品有没有相关联的套餐，如果有套餐，则抛出异常。
        //菜品和套餐是多对多的关系，可以查中间表
        List<Long> setMealIds = setMealDishMapper.getSetMealIdByDishIds(ids);
        if (setMealIds != null && setMealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3.删除菜品表中数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除相对应的口味
            dishFlavorMapper.delete(id);
        }
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Override
    public void updateDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateDish(dish);

        //考虑删除口味再添加口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null || flavors.size() != 0) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据ID查询菜品
     *
     * @param id
     * @return
     */
    @Override
    public DishVO selectById(Long id) {
        //1.查询对应ID的菜品的数据
        Dish dish = dishMapper.selectAllById(id);
        Long dishId = dish.getCategoryId();
        String categoryName = categoryMapper.selectCategortNameById(dishId);
        //2.查询菜品口味表
        List<DishFlavor> dishFlavorList = dishFlavorMapper.selectAllByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavorList);
        dishVO.setCategoryName(categoryName);
        return dishVO;
    }

    @Override
    public List<Dish> selectByCategoryId(Long categoryId) {
        List<Dish> dishList = dishMapper.selectAllByCategoryId(categoryId);
        return dishList;
    }

    /**
     * 启用或者禁用菜品状态码
     *
     * @param status
     * @param id
     */
    @Override
    public void openOrForbid(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.updateDish(dish);

        //当删除菜品时，与之相关联的套餐也要禁用
        if (status.equals(StatusConstant.DISABLE)) {
            List<Long> dishIds = new ArrayList<>();
            List<Long> setDishIds = setMealDishMapper.getSetMealIdByDishIds(dishIds);

            for (Long setDishId : setDishIds) {
                Setmeal setMeal = Setmeal.builder()
                        .status(status)
                        .id(setDishId)
                        .build();
                setMealDishMapper.update(setMeal);
            }
        }
    }
}
