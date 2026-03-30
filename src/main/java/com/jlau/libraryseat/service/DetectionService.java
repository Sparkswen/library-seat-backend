package com.jlau.libraryseat.service;

import com.jlau.libraryseat.entity.Reservation;

/**
 * 多模态检测服务接口
 * 支持 WiFi、蓝牙、RFID、摄像头等多种检测方式
 */
public interface DetectionService {

    /**
     * WiFi 连接检测
     * 检测用户是否通过图书馆 WiFi 连接
     */
    boolean detectByWifi(String studentNo, String seatNo);

    /**
     * 蓝牙信标检测
     * 检测用户是否在座位附近的蓝牙范围内
     */
    boolean detectByBluetooth(String studentNo, String seatNo);

    /**
     * RFID/NFC 检测
     * 检测用户是否刷卡签到
     */
    boolean detectByRfid(String studentNo, String seatNo, String rfidToken);

    /**
     * 摄像头人脸识别检测
     * 检测用户是否在座位区域
     */
    boolean detectByCamera(String studentNo, String seatNo);

    /**
     * 多模态融合检测
     * 综合多种检测方式判断用户是否在座
     */
    boolean multiModalDetection(String studentNo, String seatNo);

    /**
     * 开始定时检测任务
     * 用于学习时长统计
     */
    void startDetectionTask(Long reservationId);

    /**
     * 停止检测任务
     */
    void stopDetectionTask(Long reservationId);
}
