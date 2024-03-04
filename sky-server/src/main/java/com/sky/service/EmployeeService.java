package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 添加员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 禁用启动员工
     * @param status
     * @param id
     */
    void beginOrForbid(Integer status, Long id);

    /**
     * 根据ID查询员工
     * @param id
     * @return
     */
    Employee selectById(Long id);

    /**
     * 更新员工
     * @param employeeDTO
     */
    void updateEmployee(EmployeeDTO employeeDTO);
}
