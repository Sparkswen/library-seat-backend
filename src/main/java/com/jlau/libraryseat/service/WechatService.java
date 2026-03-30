package com.jlau.libraryseat.service;

/**
 * 微信小程序服务接口
 */
public interface WechatService {

    /**
     * 小程序用户登录
     * 通过 wx.login 获取 code，换取 openid 和 session_key
     */
    String miniProgramLogin(String code);

    /**
     * 获取用户信息
     */
    String getUserInfo(String openid);

    /**
     * 发送订阅消息通知
     * 如：预约成功、签到提醒、违约预警
     */
    boolean sendSubscribeMessage(String openid, String templateId, Object data);

    /**
     * 生成小程序码
     * 用于座位签到扫码
     */
    byte[] generateMiniProgramQrCode(String scene, String page);

    /**
     * 解密用户敏感数据
     * 如手机号
     */
    String decryptData(String encryptedData, String iv, String sessionKey);
}
