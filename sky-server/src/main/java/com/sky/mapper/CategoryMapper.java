package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {
    public Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 插入分类
     * @param category
     */
    void Insert(Category category);

    /**
     * 更新分类
     * @param category
     */
    void update(Category category);

    /**
     * 删除分类
     * @param id
     */
    void delete(Long id);
}
