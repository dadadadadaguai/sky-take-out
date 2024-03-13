package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    @Select("select count(id) from dish where id=category_id")
    Integer countByCategoryId(Long categoryId);

    /**
     * 分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> query(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 添加菜品
     *
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 批量删除
     *
     * @param id
     */
    @Delete("delete from dish where id=#{id}")
    void deleteById(Long id);

    @Select("select status from dish where id=#{id}")
    Integer selectById(Long id);

    /**
     * 根据 ID查询全部菜品数据
     *
     * @param id
     * @return
     */
    Dish selectAllById(Long id);

    /**
     * 根据分类ID查询菜品数据
     *
     * @param categoryId
     * @return
     */
    List<Dish> selectAllByCategoryId(Long categoryId);

    /**
     * 更新菜品表
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void updateDish(Dish dish);
}
