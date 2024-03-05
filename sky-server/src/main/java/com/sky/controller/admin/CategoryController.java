package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理
 */
@RestController
@Slf4j
@RequestMapping("/admin/category")
@Api(tags = "分类管理相关接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result Page( CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询：查询参数为:{}",categoryPageQueryDTO);
        PageResult result =categoryService.page(categoryPageQueryDTO);
        return Result.success(result);
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */

    @PostMapping
    @ApiOperation("新增分类")
    public Result AddCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类,分类数据为:{}",categoryDTO);
        categoryService.AddCategory(categoryDTO);
        return  Result.success();
    }

    /**
     * 启用禁用状态码
     * @param status
     * @param id
     * @return
     */

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用状态码")
    public Result StartOrForbid(@PathVariable Integer status,Long id){
        log.info("启用禁用状态码,状态码：{},id:{}",status,id);
        categoryService.StartOrForbid(status,id);
        return Result.success();
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类:{}",categoryDTO);
        categoryService.updateCategory(categoryDTO);
        return  Result.success();
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    public Result deleteCategory(Long id){
        categoryService.deleteCategory(id);
        return Result.success();
    }
}