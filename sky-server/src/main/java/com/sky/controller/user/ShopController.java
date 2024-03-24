package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@Slf4j
@Api(tags = "店铺相关接口")
@RequestMapping("/user/shop")
public class ShopController {
    public static final String key = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 获取店铺状态
     *
     * @return
     */
    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getShopStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        log.info("修改店铺状态，状态为：{}", status == 1 ? "营业" : "关门");
        return Result.success(status);
    }
}
