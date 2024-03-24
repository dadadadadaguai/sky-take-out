package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品处理
 */
@RestController
@Slf4j
@Api(tags = "菜品处理")
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "菜品分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult result = dishService.PageQuery(dishPageQueryDTO);
        return Result.success(result);
    }

    @PostMapping
    @ApiOperation("新增菜品")
    public Result insertDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品，dishDTO:{}",dishDTO);
        dishService.insertDish(dishDTO);
        String key="dish_" +dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("批量删除")
    public Result deleteDish(@RequestParam List<Long> ids) {
        log.info("批量删除，删除的id:{}",ids);
        dishService.deleteDish(ids);
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据ID查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("查询菜品")
    public Result<DishVO> selectById(@PathVariable Long id) {
        log.info("查询菜品，菜品的ID为:{}",id);
        DishVO result = dishService.selectById(id);
        return Result.success(result);
    }

    /**
     * 根据分类ID查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类查询菜品")
    public Result<List<Dish>> queryByCategoryId(Long categoryId) {
        log.info("分类查询菜品，菜品id为{}",categoryId);
        List<Dish> dishList = dishService.selectByCategoryId(categoryId);
        return Result.success(dishList);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品表，请求数据为:{}",dishDTO);
        dishService.updateDish(dishDTO);
        cleanCache("dish_*");
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("禁用或者启用菜品")
    public Result openOrForbid(@PathVariable Integer status,Long id){
        log.info("启用或者禁用菜品：状态码为{},id为{}",status,id);
        dishService.openOrForbid(status,id);
        cleanCache("dish_*");
        return Result.success();
    }

    private void  cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
