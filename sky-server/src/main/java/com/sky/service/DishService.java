package com.sky.service;


import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


public interface DishService {

    public PageResult PageQuery(DishPageQueryDTO dishPageQueryDTO);
}
