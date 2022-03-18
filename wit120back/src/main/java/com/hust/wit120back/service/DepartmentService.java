package com.hust.wit120back.service;

import com.hust.wit120back.dto.DepartmentDTO;
import com.hust.wit120back.dto.ShiftInfoDTO;
import com.hust.wit120back.entity.Department;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface DepartmentService {
    Integer getDepartmentId(Integer departmentId);
    ArrayList<DepartmentDTO> getDepartments();
    DepartmentDTO getDepartmentsDesc(Integer departmentId);
    ArrayList<ShiftInfoDTO> getDepartShiftInfo(Integer departmentId);
    List<String> getDepartmentName();

    Map<String, Object> getDepartmentByPage(int pageNum, int pageSize);

    boolean addDepartment(Department department);

    int updateDepartment(Department department);

    boolean deleteDepartment(Integer departmentId);
}
