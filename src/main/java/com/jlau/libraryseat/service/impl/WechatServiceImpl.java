package com.jlau.libraryseat.service.impl;

import com.jlau.libraryseat.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WechatServiceImpl implements WechatService {

    // 从配置文件读取小程序配置
    @Value("${wechat.miniapp.appid:}")
    private String appid;

    @Value("${wechat.miniapp.secret:}")
    private String secret;

    @Override
    public String miniProgramLogin(String code) {
        // TODO: 调用微信接口
        // String url = "https://api.weixin.qq.com/sns/jscode2session";
        // 参数: appid, secret, js_code=code, grant_type=authorization_code
        // 返回: openid, session_key

        log.info("[微信小程序] 登录 code: {} - 待接入微信API", code);
        return "mock_openid_" + code;
    }

    @Override
    public String getUserInfo(String openid) {
        log.info("[微信小程序] 获取用户信息: {} - 待接入", openid);
        return null;
    }

    @Override
    public boolean sendSubscribeMessage(String openid, String templateId, Object data) {
        // TODO: 调用微信订阅消息接口
        // 需要用户授权订阅

        log.info("[微信小程序] 发送订阅消息 to {} - 待接入", openid);
        return true;
    }

    @Override
    public byte[] generateMiniProgramQrCode(String scene, String page) {
        // TODO: 调用微信生成小程序码接口
        // 用于座位二维码，扫码直接进入签到页面

        log.info("[微信小程序] 生成小程序码 scene: {}, page: {} - 待接入", scene, page);
        return new byte[0];
    }

    @Override
    public String decryptData(String encryptedData, String iv, String sessionKey) {
        // TODO: 使用 AES-128-CBC 解密
        // 用于获取用户手机号

        log.info("[微信小程序] 解密数据 - 待接入");
        return null;
    }
}
