package com.jlau.libraryseat.controller;

import com.jlau.libraryseat.common.Result;
import com.jlau.libraryseat.entity.User;
import com.jlau.libraryseat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        if (userRepository.existsByStudentNo(user.getStudentNo())) {
            return Result.error("学号已存在");
        }
        user.setPassword(user.getPassword());
        userRepository.save(user);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String studentNo = params.get("studentNo");
        String password = params.get("password");

        User user = userRepository.findByStudentNo(studentNo).orElse(null);

        if (user == null || !user.getPassword().equals(password)) {
            return Result.error(401, "学号或密码错误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("user", Map.of(
                "id", user.getId(),
                "studentNo", user.getStudentNo(),
                "name", user.getName(),
                "role", user.getRole(),
                "levelTitle", user.getLevelTitle(),
                "levelDisplayName", user.getLevelTitle().getDisplayName(),
                "points", user.getPoints(),
                "creditScore", user.getCreditScore(),
                "totalStudyMinutes", user.getTotalStudyMinutes()
        ));

        return Result.success(result);
    }
}
