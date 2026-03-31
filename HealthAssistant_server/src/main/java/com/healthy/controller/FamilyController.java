package com.healthy.controller;

import com.healthy.context.BaseContext;
import com.healthy.dto.*;
import com.healthy.result.PageResult;
import com.healthy.result.Result;
import com.healthy.service.AuthorizationService;
import com.healthy.service.FamilyHealthService;
import com.healthy.service.FamilyMemberService;
import com.healthy.service.FamilyService;
import com.healthy.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/family")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "家庭管理")
public class FamilyController {

    private final FamilyService familyService;
    private final FamilyMemberService memberService;
    private final AuthorizationService authorizationService;
    private final FamilyHealthService healthService;

    // ========== 家庭基础 ==========
    @PostMapping("/create")
    @ApiOperation("创建家庭")
    public Result<FamilyVO> createFamily(@RequestBody @Valid CreateFamilyDTO dto) {
        Long userId = BaseContext.getCurrentId();
        FamilyVO vo = familyService.createFamily(userId, dto.getFamilyName());
        return Result.success(vo);
    }

    @GetMapping("/info")
    @ApiOperation("获取当前用户所属家庭信息")
    public Result<FamilyInfoVO> getFamilyInfo() {
        Long userId = BaseContext.getCurrentId();
        FamilyInfoVO vo = familyService.getFamilyInfoByUserId(userId);
        return Result.success(vo);
    }

    @PutMapping("/update")
    @ApiOperation("修改家庭名称")
    public Result<String> updateFamily(@RequestBody @Valid UpdateFamilyDTO dto) {
        Long userId = BaseContext.getCurrentId();
        familyService.updateFamilyName(userId, dto.getFamilyName());
        return Result.success();
    }

    @DeleteMapping("/dismiss")
    @ApiOperation("解散家庭")
    public Result<String> dismissFamily() {
        Long userId = BaseContext.getCurrentId();
        familyService.dismissFamily(userId);
        return Result.success();
    }

    // ========== 邀请码 ==========
    @GetMapping("/invite-code")
    @ApiOperation("获取/刷新邀请码")
    public Result<InviteCodeVO> getInviteCode(@RequestParam(defaultValue = "false") Boolean refresh) {
        Long userId = BaseContext.getCurrentId();
        String code = familyService.refreshInviteCode(userId);
        // 返回简单VO
        InviteCodeVO vo = new InviteCodeVO();
        vo.setInviteCode(code);
        vo.setExpireTime(LocalDateTime.now().plusHours(24)); // 可返回实际过期时间
        return Result.success(vo);
    }

    @PostMapping("/join")
    @ApiOperation("通过邀请码加入家庭")
    public Result<FamilyVO> joinFamily(@RequestBody @Valid JoinFamilyDTO dto) {
        Long userId = BaseContext.getCurrentId();
        FamilyVO vo = familyService.joinFamily(userId, dto.getInviteCode());
        return Result.success(vo);
    }

    // ========== 成员管理 ==========
    @GetMapping("/members")
    @ApiOperation("获取家庭成员列表")
    public Result<List<FamilyMemberVO>> getMembers() {
        Long userId = BaseContext.getCurrentId();
        List<FamilyMemberVO> list = memberService.getMembers(userId);
        return Result.success(list);
    }

    @GetMapping("/member/{userId}")
    @ApiOperation("获取成员详情")
    public Result<MemberDetailVO> getMemberDetail(@PathVariable Long userId) {
        Long currentUserId = BaseContext.getCurrentId();
        MemberDetailVO vo = memberService.getMemberDetail(currentUserId, userId);
        return Result.success(vo);
    }

    @PutMapping("/member/{userId}/relation")
    @ApiOperation("修改成员关系称谓")
    public Result<String> updateRelation(@PathVariable Long userId, @RequestBody @Valid UpdateRelationDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        memberService.updateRelation(currentUserId, userId, dto.getRelation());
        return Result.success();
    }

    @PutMapping("/member/{userId}/role")
    @ApiOperation("修改成员角色")
    public Result<String> updateRole(@PathVariable Long userId, @RequestBody @Valid UpdateRoleDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        memberService.updateRole(currentUserId, userId, dto.getRole());
        return Result.success();
    }

    @DeleteMapping("/member/{userId}")
    @ApiOperation("移除成员")
    public Result<String> removeMember(@PathVariable Long userId) {
        Long currentUserId = BaseContext.getCurrentId();
        memberService.removeMember(currentUserId, userId);
        return Result.success();
    }

    // ========== 数据授权 ==========
    @GetMapping("/authorizations")
    @ApiOperation("获取授权列表")
    public Result<List<AuthorizationVO>> getAuthorizations(@RequestParam(required = false) Long childId) {
        Long userId = BaseContext.getCurrentId();
        List<AuthorizationVO> list = authorizationService.getAuthorizations(userId, childId);
        return Result.success(list);
    }

    @PostMapping("/authorize")
    @ApiOperation("设置授权")
    public Result<Long> setAuthorization(@RequestBody @Valid SetAuthorizationDTO dto) {
        Long userId = BaseContext.getCurrentId();
        Long authId = authorizationService.setAuthorization(userId, dto);
        return Result.success(authId);
    }

    @DeleteMapping("/authorize/{authId}")
    @ApiOperation("撤销授权")
    public Result<String> revokeAuthorization(@PathVariable Long authId) {
        Long userId = BaseContext.getCurrentId();
        authorizationService.revokeAuthorization(userId, authId);
        return Result.success();
    }

    // ========== 家庭健康 ==========
    @GetMapping("/health/overview")
    @ApiOperation("获取家庭成员健康概览")
    public Result<List<HealthOverviewVO>> getHealthOverview() {
        Long userId = BaseContext.getCurrentId();
        List<HealthOverviewVO> list = healthService.getHealthOverview(userId);
        return Result.success(list);
    }

    @GetMapping("/health/history")
    @ApiOperation("获取指定成员健康历史")
    public Result<PageResult> getHealthHistory(
            @RequestParam Long userId,
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long currentUserId = BaseContext.getCurrentId();
        PageResult page = healthService.getHealthHistory(currentUserId, userId, type, startDate, endDate, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/report")
    @ApiOperation("获取家庭健康报告")
    public Result<FamilyReportVO> getFamilyReport(@RequestParam(defaultValue = "month") String period) {
        Long userId = BaseContext.getCurrentId();
        FamilyReportVO report = healthService.generateFamilyReport(userId, period);
        return Result.success(report);
    }

    @GetMapping("/emergency-contacts")
    @ApiOperation("获取紧急联系人（家长电话）")
    public Result<List<EmergencyContactVO>> getEmergencyContacts() {
        Long userId = BaseContext.getCurrentId();
        List<EmergencyContactVO> list = healthService.getEmergencyContacts(userId);
        return Result.success(list);
    }
}
