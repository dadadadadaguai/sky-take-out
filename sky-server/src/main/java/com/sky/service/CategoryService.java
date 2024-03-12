package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
     void StartOrForbid(Integer status, Long id);

    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    void AddCategory(CategoryDTO categoryDTO);

    void updateCategory(CategoryDTO categoryDTO);

    void deleteCategory(Long id);

    List<Category> list(Integer type);


}
