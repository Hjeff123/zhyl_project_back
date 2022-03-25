package com.hust.wit120back.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.hust.wit120back.common.Constants;
import com.hust.wit120back.dto.MedResOrderDTO;
import com.hust.wit120back.exception.ServiceException;
import com.hust.wit120back.mapper.*;
import com.hust.wit120back.service.MedicalResourceService;
import com.hust.wit120back.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.relation.RelationSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MedicalResourceServiceImpl implements MedicalResourceService {
    @Autowired
    private PatientInfoMapper patientInfoMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MedicalTechnicianMapper medicalTechnicianMapper;

    @Autowired
    private MedicalResourceOrderMapper medicalResourceOrderMapper;

    @Autowired
    private ResourceRecommendMapper resourceRecommendMapper;

    @Autowired
    private DocInfoMapper docInfoMapper;

    @Autowired
    private CheckResultMapper checkResultMapper;

    @Override
    public void checkParameter(MedResOrderDTO medResOrderDTO){
        //check patientId, orderId, medResName, day, noon, cost
        Integer patientId = medResOrderDTO.getPatientId();
        Integer orderId = medResOrderDTO.getOrderId();
        String medResName = medResOrderDTO.getMedResName();
        Integer medResId = medicalTechnicianMapper.selectTechnicianName(medResName);
        int day = medResOrderDTO.getDay();
        int noon = medResOrderDTO.getNoon();
        int cost = medResOrderDTO.getCost();
        if(patientInfoMapper.selectPatientId(patientId) == null)
            throw new ServiceException(Constants.CODE_600, "该患者信息不存在");
        else if(orderMapper.selectOrderId(orderId) == null)
            throw new ServiceException(Constants.CODE_600, "挂号信息不存在");
        else if(StrUtil.isBlank(medResName) || medResId == null)
            throw new ServiceException(Constants.CODE_400, "设备名错误");
        else if(day < 0 || day > 6 || !(noon == 1 || noon == 2))
            throw new ServiceException(Constants.CODE_400, "预约日期错误");
        //查询是否已经预约
        if(medicalResourceOrderMapper.selectMedResOrderId(orderId, medResId) != null)
            throw new ServiceException(Constants.CODE_502, "该用户已预约");
        //是否还有预约名额
        if(medicalResourceOrderMapper.selectOrderNumber(day, noon, medResId) >= 12)
            throw new ServiceException(Constants.CODE_501, "该时段已无预约名额");
    }

    @Override
    public void addAppointment(MedResOrderDTO medResOrderDTO){
        int cost = medicalTechnicianMapper.selectCost(medResOrderDTO.getMedResName());
        medResOrderDTO.setCost(cost);
        Integer medResId = medicalTechnicianMapper.selectTechnicianName(medResOrderDTO.getMedResName());
        medResOrderDTO.setMedResId(medResId);
        medicalResourceOrderMapper.addAppointment(medResOrderDTO);
    }

    @Override
    public List<MedResOrderDTO> getAllMedResAppointment(Integer patientId){
        List<MedResOrderDTO> medResOrders;
        medResOrders = medicalResourceOrderMapper.selectAllMedResOrdersByPatientId(patientId);
        for(MedResOrderDTO medResOrder : medResOrders){
            //设置医技资源的名称
            medResOrder.setMedResName(medicalTechnicianMapper.selectTechnicianNameById(medResOrder.getMedResId()));
            //设置预约日期
            String date = TimeUtils.getOrderDate(medResOrder.getCreateTime(), medResOrder.getDay());
            medResOrder.setOrderTime(date);
        }
        //sort
        Collections.sort(medResOrders);
        return medResOrders;
    }

    @Override
    public List<MedResOrderDTO> getMedResAppointmentByOrderId(Integer orderId){
        List<MedResOrderDTO> medResOrders;
        medResOrders = medicalResourceOrderMapper.selectMedResOrderByOrderId(orderId);
        for(MedResOrderDTO medResOrder : medResOrders){
            //设置医技资源的名称
            medResOrder.setMedResName(medicalTechnicianMapper.selectTechnicianNameById(medResOrder.getMedResId()));
            //设置预约日期
            String date = TimeUtils.getOrderDate(medResOrder.getCreateTime(), medResOrder.getDay());
            medResOrder.setOrderTime(date);
        }
        return medResOrders;
    }

    @Override
    public boolean addMedResRecommend(Integer orderId, String recommend){
        if(resourceRecommendMapper.selectOrderId(orderId) == null){
            resourceRecommendMapper.addRecommend(orderId, recommend);
        }else{
            resourceRecommendMapper.updateRecommend(orderId, recommend);
        }
        return true;
    }

    @Override
    public List<Map<String, Integer>> getMedResNameAndId(){
        return medicalTechnicianMapper.selectTechniciansNameAndId();
    }

    @Override
    public String getMedResRecommend(Integer orderId){
        String recommend = resourceRecommendMapper.selectRecommendByOrderId(orderId);
        if(StrUtil.isBlank(recommend))
            throw new ServiceException(Constants.CODE_600, "无医技推荐");
        return recommend;
    }

    @Override
    public List<MedResOrderDTO> getMedResOrderByIdAndDate(Integer doctorId, String date){
        if(docInfoMapper.selectDoctorId(doctorId) == null)
            throw new ServiceException(Constants.CODE_600, "不存在该医生");
        Integer medResId = medicalTechnicianMapper.selectTechnicianIdByDocId(doctorId);
        String medResName = medicalTechnicianMapper.selectTechnicianNameById(medResId);
        List<MedResOrderDTO> medResOrders = medicalResourceOrderMapper.selectMedResOrderByMedResId(medResId);
        List<MedResOrderDTO> todayMedResOrders = new ArrayList<MedResOrderDTO>();
        for(MedResOrderDTO order : medResOrders){
            //设置预约日期
            String orderDate = TimeUtils.getOrderDate(order.getCreateTime(), order.getDay());
            if(!orderDate.equals(date)) continue;
            order.setOrderTime(date);
            //设置医技资源名称
            order.setMedResName(medResName);
            //设置患者姓名
            order.setPatientName(patientInfoMapper.selectRealNameById(order.getPatientId()));
            //是否处理
            if(StrUtil.isBlank(checkResultMapper.selectCheckResultByMedResOrderId(order.getMedResOrderId())))
                order.setDeal(false);
            else
                order.setDeal(true);
            todayMedResOrders.add(order);
        }
        return todayMedResOrders;
    }

    @Override
    public String getCheckResult(Integer medResOrderId){
        String checkResult = checkResultMapper.selectCheckResultByMedResOrderId(medResOrderId);
        if(StrUtil.isBlank(checkResult))
            throw new ServiceException(Constants.CODE_600, "无查询结果");
        return checkResult;
    }

    @Override
    public boolean addCheckResult(Integer medResOrderId, String checkResult){
        if(medicalResourceOrderMapper.selectMedResOrderIdByItself(medResOrderId) == null)
            throw new ServiceException(Constants.CODE_600, "该医技预约单不存在");
        checkResultMapper.addCheckResult(medResOrderId, checkResult);
        return true;
    }
}
