package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;

public interface CategoryService {
     void StartOrForbid(Integer status, Long id);

    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    void AddCategory(CategoryDTO categoryDTO);

    void updateCategory(CategoryDTO categoryDTO);

    void deleteCategory(Long id);
}
