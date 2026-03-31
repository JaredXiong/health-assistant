package com.healthy.controller;

import com.healthy.context.BaseContext;
import com.healthy.dto.UserMedicineDTO;
import com.healthy.result.Result;
import com.healthy.service.UserMedicineService;
import com.healthy.vo.UserMedicineVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户药品管理控制器
 */
@RestController
@RequestMapping("/user-medicine")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "用户药品管理")
public class UserMedicineController {

    private final UserMedicineService userMedicineService;

    /**
     * 将识别药品加入用户管理
     */
    @PostMapping("/add")
    @ApiOperation("将识别药品加入管理")
    public Result<Long> addUserMedicine(@RequestBody @Valid UserMedicineDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        log.info("用户添加药品管理：userId={}, dto={}", userId, dto);
        Long id = userMedicineService.addUserMedicine(userId, dto.getMedicineId(), dto);
        return Result.success(id);
    }

    /**
     * 查询用户药品列表（可按状态筛选）
     */
    @GetMapping("/list")
    @ApiOperation("查询用户药品列表")
    public Result<List<UserMedicineVO>> getUserMedicines(
            @ApiParam("状态筛选（PLACED/USING/STANDBY/EXPIRED，为空则查全部）") @RequestParam(required = false) String status) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        List<UserMedicineVO> list = userMedicineService.getUserMedicines(userId, status);
        return Result.success(list);
    }

    /**
     * 更新用户药品信息（如修改剂量、频率等）
     */
    @PutMapping("/update/{id}")
    @ApiOperation("更新用户药品信息")
    public Result<String> updateUserMedicine(
            @ApiParam("用户药品ID") @PathVariable Long id,
            @RequestBody @Valid UserMedicineDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        // 确保 dto 中携带需要更新的字段，且 id 来自路径
        dto.setMedicineId(null); // 不允许修改关联的药品ID，强制设为 null
        userMedicineService.updateUserMedicine(id, dto);
        return Result.success("更新成功");
    }

    /**
     * 删除用户药品记录
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除用户药品记录")
    public Result<String> removeUserMedicine(@ApiParam("用户药品ID") @PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        userMedicineService.removeUserMedicine(id);
        return Result.success("删除成功");
    }

    /**
     * 激活放置状态的药品为使用状态
     */
    @PostMapping("/activate")
    @ApiOperation("激活药品为使用状态")
    public Result<String> activateMedicine(
            @RequestParam Long userMedicineId,
            @RequestParam(required = false) String dosagePerTime,
            @RequestParam String frequency,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        userMedicineService.activateMedicine(userMedicineId, dosagePerTime, frequency, startDate, endDate);
        return Result.success("激活成功");
    }

    @PostMapping("/from-prescription/{itemId}")
    @ApiOperation("从处方明细添加到药箱")
    public Result<Long> addFromPrescription(@PathVariable Long itemId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        Long userMedicineId = userMedicineService.addFromPrescription(itemId, userId);
        return Result.success(userMedicineId);
    }
}