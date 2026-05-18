package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.annotation.RateLimiter;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.redis.RedisCache;
import com.tcmseek.common.exception.ServiceException;
import com.tcmseek.common.utils.SecurityUtils;
import com.tcmseek.common.utils.StringUtils;
import com.tcmseek.pojo.LoginRequest;
import com.tcmseek.pojo.LoginResponse;
import com.tcmseek.pojo.SendCodeRequest;
import com.tcmseek.pojo.UserWeb;
import com.tcmseek.service.EmailService;
import com.tcmseek.service.UserWebService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/tcmseek")
@Slf4j
@Api(tags = "用户认证")
public class LoginController {




    @Autowired
    private UserWebService userWebService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisCache redisCache;

    /** 验证码Redis key前缀 */
    private static final String VERIFY_CODE_KEY = "verify_code:";

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 统一结果
     */
    @RateLimiter(count = 5, time = 60)
    @Anonymous
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户账号密码登录，返回JWT Token")
    public AjaxResult login (@RequestBody LoginRequest loginRequest) {
        AjaxResult ajax =   AjaxResult.success();
        if (loginRequest.getAccount() == null || loginRequest.getAccount().trim().isEmpty()) {
            return AjaxResult.error("账号不能为空");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return AjaxResult.error("密码不能为空");
        }

        LoginResponse response = userWebService.login(loginRequest);
        if (response == null) {
            ajax = AjaxResult.error("账号或密码错误");
        } else {
            ajax.put("data", response);
        }
        return ajax;
    }

    /**
     * 校验用户端Token，供Gateway转发AI请求前调用。
     */
    @GetMapping("/auth/validate")
    @ApiOperation(value = "校验用户Token", notes = "校验用户端登录Token，返回当前用户基础信息")
    public AjaxResult validateToken() {
        com.tcmseek.common.core.domain.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        com.tcmseek.common.core.domain.entity.SysUser sysUser = loginUser.getUser();
        Map<String, Object> user = new LinkedHashMap<>();
        if (sysUser != null) {
            user.put("id", sysUser.getUserId());
            user.put("username", sysUser.getUserName());
            user.put("account", sysUser.getNickName());
            user.put("email", sysUser.getEmail());
        } else {
            user.put("id", loginUser.getUserId());
            user.put("username", loginUser.getUsername());
        }
        return AjaxResult.success(user);
    }

    /**
     * 发送邮箱验证码
     * @param request 发送验证码请求
     * @return 统一结果
     */
    @Anonymous
    @PostMapping("/sendCode")
    @ApiOperation(value = "发送邮箱验证码", notes = "发送6位数字验证码到指定邮箱，有效期5分钟，60秒内只能发送一次")
    public AjaxResult sendCode(@RequestBody SendCodeRequest request) {
        // 参数校验
        if (StringUtils.isEmpty(request.getEmail())) {
            return AjaxResult.error("邮箱不能为空");
        }
        // 邮箱格式校验
        if (!request.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return AjaxResult.error("邮箱格式不正确");
        }

        // 检查是否频繁发送（60秒内只能发送一次）
        String codeKey = VERIFY_CODE_KEY + request.getEmail();
        if (redisCache.hasKey(codeKey)) {
            Long expire = redisCache.getExpire(codeKey);
            if (expire != null && expire > 240) { // 5分钟-4分钟=1分钟内不能重复发送
                return AjaxResult.error("验证码已发送，请稍后再试");
            }
        }

        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 发送邮件
        boolean sent = emailService.sendVerificationCode(request.getEmail(), code);
        if (!sent) {
            return AjaxResult.error("验证码发送失败，请稍后再试");
        }

        // 将验证码存入Redis，有效期5分钟
        redisCache.setCacheObject(codeKey, code, 5, TimeUnit.MINUTES);

        return AjaxResult.success("验证码已发送，请查收邮件");
    }

    /**
     * 用户注册
     * @param userWeb 注册信息
     * @return 统一结果
     */
    @RateLimiter(count = 5, time = 60)
    @Anonymous
    @PostMapping("/regist")
    @ApiOperation(value = "用户注册", notes = "用户注册接口，需要邮箱验证码")
    public AjaxResult regist(@RequestBody UserWeb userWeb) {
        // 参数校验（先校验再加密）
        if (StringUtils.isEmpty(userWeb.getUsername())) {
            return AjaxResult.error("用户名不能为空");
        }
        if (StringUtils.isEmpty(userWeb.getAccount())) {
            return AjaxResult.error("账号不能为空");
        }
        if (StringUtils.isEmpty(userWeb.getPassword())) {
            return AjaxResult.error("密码不能为空");
        }
        
        // 密码加密（在校验之后加密）
        userWeb.setPassword(SecurityUtils.encryptPassword(userWeb.getPassword()));
        if (StringUtils.isEmpty(userWeb.getEmail())) {
            return AjaxResult.error("邮箱不能为空");
        }
        // 邮箱格式校验
        if (!userWeb.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return AjaxResult.error("邮箱格式不正确");
        }
        // 验证码校验
        if (StringUtils.isEmpty(userWeb.getCode())) {
            return AjaxResult.error("验证码不能为空");
        }

        // 从Redis获取验证码
        String codeKey = VERIFY_CODE_KEY + userWeb.getEmail();
        String cachedCode = redisCache.getCacheObject(codeKey);
        
        if (StringUtils.isEmpty(cachedCode)) {
            return AjaxResult.error("验证码已过期，请重新获取");
        }
        
        if (!cachedCode.equals(userWeb.getCode())) {
            return AjaxResult.error("验证码错误");
        }

        try {
            // 执行注册
            boolean result = userWebService.regist(userWeb);
            if (!result) {
                return AjaxResult.error("注册失败");
            }
            
            // 注册成功后删除验证码
            redisCache.deleteObject(codeKey);

            //向3dstarpred.pumc.wecomput.com/注册绑定账号
            //密码统一Gst@176439364

            String url = "https://3dstarpred.pumc.wecomput.com/api/user/register";

            // 请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("Name", userWeb.getAccount());
            params.put("Email", userWeb.getEmail());
            params.put("Passwd", "Gst@176439364");
            params.put("CompanyName", "");

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 组合请求体
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);

            // 发送 POST 请求
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(url, request, String.class);

            log.info(response);

            return AjaxResult.success("注册成功");
        } catch (ServiceException e) {
            // 捕获业务异常，返回友好提示
            return AjaxResult.error(e.getMessage());
        }
    }
}
