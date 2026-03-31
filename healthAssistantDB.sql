-- 健康助手系统数据库脚本
-- 创建时间: 2026-03-31
-- 数据库版本: MySQL 8.0+

-- ==================== 创建数据库 ====================
DROP DATABASE IF EXISTS `health_assistant`;
CREATE DATABASE `health_assistant` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `health_assistant`;

-- ==================== 创建表结构 ====================

-- 1. 用户表
CREATE TABLE `users` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                         `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
                         `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
                         `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                         `nickname` VARCHAR(100) DEFAULT NULL COMMENT '微信昵称',
                         `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_openid` (`openid`),
                         KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 家庭表
CREATE TABLE `family` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                          `name` VARCHAR(100) NOT NULL COMMENT '家庭名称',
                          `invite_code` VARCHAR(20) NOT NULL COMMENT '邀请码',
                          `invite_code_expire` DATETIME DEFAULT NULL COMMENT '邀请码过期时间',
                          `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID',
                          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_invite_code` (`invite_code`),
                          KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭表';

-- 3. 家庭成员表
CREATE TABLE `family_member` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `family_id` BIGINT NOT NULL COMMENT '家庭ID',
                                 `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                 `role` VARCHAR(20) NOT NULL COMMENT '角色: PARENT-家长, CHILD-孩子',
                                 `relation` VARCHAR(50) DEFAULT NULL COMMENT '关系称谓',
                                 `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                                 `status` TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-已移除',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_family_id` (`family_id`),
                                 KEY `idx_user_id` (`user_id`),
                                 KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭成员表';

-- 4. 健康数据表
CREATE TABLE `health_data` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `user_id` BIGINT NOT NULL COMMENT '用户ID',
                               `heart_rate` INT DEFAULT NULL COMMENT '心率(次/分钟)',
                               `systolic_bp` INT DEFAULT NULL COMMENT '收缩压(mmHg)',
                               `diastolic_bp` INT DEFAULT NULL COMMENT '舒张压(mmHg)',
                               `blood_oxygen` INT DEFAULT NULL COMMENT '血氧饱和度(%)',
                               `blood_sugar` DECIMAL(5,2) DEFAULT NULL COMMENT '血糖(mmol/L)',
                               `body_temperature` DECIMAL(4,2) DEFAULT NULL COMMENT '体温(℃)',
                               `respiratory_rate` INT DEFAULT NULL COMMENT '呼吸频率(次/分钟)',
                               `measurement_time` DATETIME NOT NULL COMMENT '测量时间',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_user_id` (`user_id`),
                               KEY `idx_measurement_time` (`measurement_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康数据表';

-- 5. 健康报告表
CREATE TABLE `health_report` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                 `health_data_id` BIGINT NOT NULL COMMENT '健康数据ID',
                                 `overall_score` INT DEFAULT NULL COMMENT '总体评分(0-100)',
                                 `health_level` VARCHAR(20) DEFAULT NULL COMMENT '健康等级: 优秀/良好/一般/较差',
                                 `blood_pressure_eval` VARCHAR(200) DEFAULT NULL COMMENT '血压评价',
                                 `blood_sugar_eval` VARCHAR(200) DEFAULT NULL COMMENT '血糖评价',
                                 `heart_rate_eval` VARCHAR(200) DEFAULT NULL COMMENT '心率评价',
                                 `blood_oxygen_eval` VARCHAR(200) DEFAULT NULL COMMENT '血氧评价',
                                 `body_temperature_eval` VARCHAR(200) DEFAULT NULL COMMENT '体温评价',
                                 `respiratory_rate_eval` VARCHAR(200) DEFAULT NULL COMMENT '呼吸频率评价',
                                 `risk_factors` TEXT DEFAULT NULL COMMENT '风险因素',
                                 `recommendations` TEXT DEFAULT NULL COMMENT '健康建议',
                                 `generate_time` DATETIME DEFAULT NULL COMMENT '生成时间',
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_user_id` (`user_id`),
                                 KEY `idx_health_data_id` (`health_data_id`),
                                 KEY `idx_generate_time` (`generate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康报告表';

-- 6. 药品信息表
CREATE TABLE `medicine_info` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `name` VARCHAR(200) NOT NULL COMMENT '药品名称',
                                 `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
                                 `manufacturer` VARCHAR(200) DEFAULT NULL COMMENT '生产厂家',
                                 `approval_number` VARCHAR(100) DEFAULT NULL COMMENT '批准文号',
                                 `usage_dosage` VARCHAR(500) DEFAULT NULL COMMENT '用法用量',
                                 `ingredients` TEXT DEFAULT NULL COMMENT '成份',
                                 `indications` TEXT DEFAULT NULL COMMENT '功能主治',
                                 `adverse_reactions` TEXT DEFAULT NULL COMMENT '不良反应',
                                 `precautions` TEXT DEFAULT NULL COMMENT '注意事项',
                                 `contraindications` TEXT DEFAULT NULL COMMENT '禁忌症',
                                 `expiry_date` DATE DEFAULT NULL COMMENT '有效期',
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_name` (`name`),
                                 KEY `idx_approval_number` (`approval_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品信息表';

-- 7. 用户药品表
CREATE TABLE `user_medicine` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                 `medicine_id` BIGINT NOT NULL COMMENT '药品ID',
                                 `status` VARCHAR(20) DEFAULT 'STANDBY' COMMENT '状态: PLACED-已放置, USING-使用中, STANDBY-备用, EXPIRED-已过期',
                                 `dosage_per_time` VARCHAR(50) DEFAULT NULL COMMENT '每次用量',
                                 `frequency` VARCHAR(100) DEFAULT NULL COMMENT '服用频率',
                                 `start_date` DATE DEFAULT NULL COMMENT '开始日期',
                                 `end_date` DATE DEFAULT NULL COMMENT '结束日期',
                                 `reminder_enabled` TINYINT DEFAULT 0 COMMENT '是否启用提醒: 0-否, 1-是',
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_user_id` (`user_id`),
                                 KEY `idx_medicine_id` (`medicine_id`),
                                 KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户药品表';

-- 8. 药品图片识别记录表
CREATE TABLE `medicine_image_record` (
                                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                         `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                         `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
                                         `ocr_raw_result` TEXT DEFAULT NULL COMMENT 'OCR原始识别结果',
                                         `parsed_info` TEXT DEFAULT NULL COMMENT '解析后的药品信息',
                                         `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待处理, 1-成功, 2-失败',
                                         `ingredients` TEXT DEFAULT NULL COMMENT '识别的成份',
                                         `indications` TEXT DEFAULT NULL COMMENT '识别的功能主治',
                                         `adverse_reactions` TEXT DEFAULT NULL COMMENT '识别的不良反应',
                                         `precautions` TEXT DEFAULT NULL COMMENT '识别的注意事项',
                                         `contraindications` TEXT DEFAULT NULL COMMENT '识别的禁忌症',
                                         `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         PRIMARY KEY (`id`),
                                         KEY `idx_user_id` (`user_id`),
                                         KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品图片识别记录表';

-- 9. 处方记录表
CREATE TABLE `prescription_records` (
                                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                        `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                        `visit_date` DATE DEFAULT NULL COMMENT '就诊日期',
                                        `hospital` VARCHAR(200) DEFAULT NULL COMMENT '医院名称',
                                        `department` VARCHAR(100) DEFAULT NULL COMMENT '科室',
                                        `doctor` VARCHAR(100) DEFAULT NULL COMMENT '医生姓名',
                                        `notes` TEXT DEFAULT NULL COMMENT '备注',
                                        `photos` JSON DEFAULT NULL COMMENT '处方照片URL列表',
                                        `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        PRIMARY KEY (`id`),
                                        KEY `idx_user_id` (`user_id`),
                                        KEY `idx_visit_date` (`visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方记录表';

-- 10. 处方项目表
CREATE TABLE `prescription_items` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `record_id` BIGINT NOT NULL COMMENT '处方记录ID',
                                      `medicine_name` VARCHAR(200) NOT NULL COMMENT '药品名称',
                                      `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
                                      `dosage_per_time` VARCHAR(50) DEFAULT NULL COMMENT '每次用量',
                                      `frequency` VARCHAR(100) DEFAULT NULL COMMENT '服用频率',
                                      `total_amount` VARCHAR(50) DEFAULT NULL COMMENT '总用量',
                                      `usage` VARCHAR(500) DEFAULT NULL COMMENT '用法',
                                      `notes` TEXT DEFAULT NULL COMMENT '备注',
                                      `user_medicine_id` BIGINT DEFAULT NULL COMMENT '关联的用户药品ID',
                                      `start_date` DATE DEFAULT NULL COMMENT '建议开始日期',
                                      `end_date` DATE DEFAULT NULL COMMENT '建议结束日期',
                                      `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_record_id` (`record_id`),
                                      KEY `idx_user_medicine_id` (`user_medicine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方项目表';

-- 11. 提醒表
CREATE TABLE `reminder` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `user_medicine_id` BIGINT NOT NULL COMMENT '用户药品ID',
                            `dosage` VARCHAR(50) DEFAULT NULL COMMENT '每次用量',
                            `reminder_time` TIME NOT NULL COMMENT '提醒时间',
                            `days_of_week` VARCHAR(50) DEFAULT NULL COMMENT '星期几: 1-7,逗号分隔',
                            `start_date` DATE DEFAULT NULL COMMENT '开始日期',
                            `end_date` DATE DEFAULT NULL COMMENT '结束日期',
                            `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待推送, 1-已推送, 2-已取消',
                            `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_user_medicine_id` (`user_medicine_id`),
                            KEY `idx_reminder_time` (`reminder_time`),
                            KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒表';

-- 12. 数据授权表
CREATE TABLE `data_authorization` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `family_id` BIGINT NOT NULL COMMENT '家庭ID',
                                      `grantor_id` BIGINT NOT NULL COMMENT '授权者ID(家长)',
                                      `grantee_id` BIGINT NOT NULL COMMENT '被授权者ID(孩子)',
                                      `data_types` JSON DEFAULT NULL COMMENT '授权的数据类型: ["heartRate","bloodPressure"]',
                                      `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
                                      `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_family_id` (`family_id`),
                                      KEY `idx_grantor_id` (`grantor_id`),
                                      KEY `idx_grantee_id` (`grantee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据授权表';

-- 13. 对话记录表
CREATE TABLE `conversation` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                `session_id` VARCHAR(100) NOT NULL COMMENT '会话ID',
                                `role` VARCHAR(20) NOT NULL COMMENT '角色: user或assistant',
                                `content` TEXT NOT NULL COMMENT '对话内容',
                                `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_user_id` (`user_id`),
                                KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话记录表';


-- ==================== 创建视图 ====================

-- 创建用户健康概览视图
CREATE OR REPLACE VIEW `v_user_health_overview` AS
SELECT
    u.id AS user_id,
    u.name AS user_name,
    u.nickname,
    hd.heart_rate,
    hd.systolic_bp,
    hd.diastolic_bp,
    hd.blood_oxygen,
    hd.blood_sugar,
    hd.body_temperature,
    hd.respiratory_rate,
    hd.measurement_time,
    hr.overall_score,
    hr.health_level
FROM users u
         LEFT JOIN health_data hd ON u.id = hd.user_id
         LEFT JOIN health_report hr ON hd.id = hr.health_data_id
WHERE hd.measurement_time = (
    SELECT MAX(measurement_time)
    FROM health_data
    WHERE user_id = u.id
);

-- 创建家庭健康统计视图
CREATE OR REPLACE VIEW `v_family_health_stats` AS
SELECT
    f.id AS family_id,
    f.name AS family_name,
    COUNT(DISTINCT fm.user_id) AS member_count,
    AVG(hd.heart_rate) AS avg_heart_rate,
    AVG(hd.systolic_bp) AS avg_systolic_bp,
    AVG(hd.diastolic_bp) AS avg_diastolic_bp,
    AVG(hd.blood_sugar) AS avg_blood_sugar,
    COUNT(DISTINCT hd.id) AS total_health_records
FROM family f
         LEFT JOIN family_member fm ON f.id = fm.family_id AND fm.status = 1
         LEFT JOIN health_data hd ON fm.user_id = hd.user_id
GROUP BY f.id, f.name;

-- 创建用户药品提醒视图
CREATE OR REPLACE VIEW `v_user_medicine_reminders` AS
SELECT
    u.id AS user_id,
    u.name AS user_name,
    mi.name AS medicine_name,
    um.dosage_per_time,
    um.frequency,
    r.reminder_time,
    r.days_of_week,
    r.status AS reminder_status,
    um.status AS medicine_status
FROM users u
         JOIN user_medicine um ON u.id = um.user_id
         JOIN medicine_info mi ON um.medicine_id = mi.id
         LEFT JOIN reminder r ON um.id = r.user_medicine_id
WHERE um.status IN ('USING', 'STANDBY');

-- ==================== 创建存储过程 ====================

DELIMITER //

-- 清理过期提醒的存储过程
CREATE PROCEDURE `sp_clean_expired_reminders`()
BEGIN
    UPDATE reminder
    SET status = 2
    WHERE end_date < CURDATE() AND status = 0;
END //

-- 清理过期药品的存储过程
CREATE PROCEDURE `sp_clean_expired_medicines`()
BEGIN
    UPDATE user_medicine
    SET status = 'EXPIRED'
    WHERE end_date < CURDATE() AND status != 'EXPIRED';
END //

-- 生成健康报告的存储过程
CREATE PROCEDURE `sp_generate_health_report`(IN p_user_id BIGINT, IN p_health_data_id BIGINT)
BEGIN
    DECLARE v_heart_rate INT;
    DECLARE v_systolic_bp INT;
    DECLARE v_diastolic_bp INT;
    DECLARE v_blood_oxygen INT;
    DECLARE v_blood_sugar DECIMAL(5,2);
    DECLARE v_body_temperature DECIMAL(4,2);
    DECLARE v_respiratory_rate INT;
    DECLARE v_overall_score INT;
    DECLARE v_health_level VARCHAR(20);
    DECLARE v_bp_eval VARCHAR(200);
    DECLARE v_bs_eval VARCHAR(200);
    DECLARE v_hr_eval VARCHAR(200);
    DECLARE v_bo_eval VARCHAR(200);
    DECLARE v_bt_eval VARCHAR(200);
    DECLARE v_rr_eval VARCHAR(200);
    DECLARE v_risk_factors TEXT;
    DECLARE v_recommendations TEXT;

    -- 获取健康数据
    SELECT heart_rate, systolic_bp, diastolic_bp, blood_oxygen, blood_sugar,
           body_temperature, respiratory_rate
    INTO v_heart_rate, v_systolic_bp, v_diastolic_bp, v_blood_oxygen,
        v_blood_sugar, v_body_temperature, v_respiratory_rate
    FROM health_data
    WHERE id = p_health_data_id;

    -- 计算评分和评价
    SET v_overall_score = 100;
    SET v_health_level = '优秀';
    SET v_risk_factors = '';
    SET v_recommendations = '继续保持健康的生活方式。';

    -- 血压评价
    IF v_systolic_bp >= 140 OR v_diastolic_bp >= 90 THEN
        SET v_bp_eval = '血压偏高，建议关注。';
        SET v_overall_score = v_overall_score - 20;
        SET v_health_level = '一般';
        SET v_risk_factors = CONCAT(v_risk_factors, '血压偏高；');
        SET v_recommendations = CONCAT(v_recommendations, '建议低盐饮食，适量运动。');
    ELSEIF v_systolic_bp >= 120 OR v_diastolic_bp >= 80 THEN
        SET v_bp_eval = '血压处于正常高值，建议关注。';
        SET v_overall_score = v_overall_score - 10;
    ELSE
        SET v_bp_eval = '血压正常，处于理想范围。';
    END IF;

    -- 血糖评价
    IF v_blood_sugar >= 7.0 THEN
        SET v_bs_eval = '血糖偏高，建议控制饮食。';
        SET v_overall_score = v_overall_score - 20;
        SET v_health_level = '一般';
        SET v_risk_factors = CONCAT(v_risk_factors, '血糖偏高；');
        SET v_recommendations = CONCAT(v_recommendations, '建议控制碳水化合物摄入。');
    ELSEIF v_blood_sugar >= 6.1 THEN
        SET v_bs_eval = '血糖处于正常高值，建议关注。';
        SET v_overall_score = v_overall_score - 10;
    ELSE
        SET v_bs_eval = '血糖正常，处于健康范围。';
    END IF;

    -- 心率评价
    IF v_heart_rate > 100 OR v_heart_rate < 60 THEN
        SET v_hr_eval = '心率异常，建议咨询医生。';
        SET v_overall_score = v_overall_score - 15;
        SET v_health_level = '一般';
        SET v_risk_factors = CONCAT(v_risk_factors, '心率异常；');
    ELSE
        SET v_hr_eval = '心率正常，处于健康范围。';
    END IF;

    -- 血氧评价
    IF v_blood_oxygen < 95 THEN
        SET v_bo_eval = '血氧偏低，建议就医。';
        SET v_overall_score = v_overall_score - 15;
        SET v_health_level = '较差';
        SET v_risk_factors = CONCAT(v_risk_factors, '血氧偏低；');
    ELSE
        SET v_bo_eval = '血氧正常，处于健康范围。';
    END IF;

    -- 体温评价
    IF v_body_temperature > 37.3 THEN
        SET v_bt_eval = '体温偏高，可能有发热。';
        SET v_overall_score = v_overall_score - 10;
        SET v_health_level = '一般';
        SET v_risk_factors = CONCAT(v_risk_factors, '体温偏高；');
    ELSEIF v_body_temperature < 36.0 THEN
        SET v_bt_eval = '体温偏低，注意保暖。';
        SET v_overall_score = v_overall_score - 5;
    ELSE
        SET v_bt_eval = '体温正常，处于健康范围。';
    END IF;

    -- 呼吸频率评价
    IF v_respiratory_rate > 24 OR v_respiratory_rate < 12 THEN
        SET v_rr_eval = '呼吸频率异常，建议咨询医生。';
        SET v_overall_score = v_overall_score - 10;
        SET v_health_level = '一般';
        SET v_risk_factors = CONCAT(v_risk_factors, '呼吸频率异常；');
    ELSE
        SET v_rr_eval = '呼吸频率正常，处于健康范围。';
    END IF;

    -- 确定健康等级
    IF v_overall_score >= 90 THEN
        SET v_health_level = '优秀';
    ELSEIF v_overall_score >= 80 THEN
        SET v_health_level = '良好';
    ELSEIF v_overall_score >= 70 THEN
        SET v_health_level = '一般';
    ELSE
        SET v_health_level = '较差';
    END IF;

    -- 插入健康报告
    INSERT INTO health_report (
        user_id, health_data_id, overall_score, health_level,
        blood_pressure_eval, blood_sugar_eval, heart_rate_eval,
        blood_oxygen_eval, body_temperature_eval, respiratory_rate_eval,
        risk_factors, recommendations, generate_time
    ) VALUES (
                 p_user_id, p_health_data_id, v_overall_score, v_health_level,
                 v_bp_eval, v_bs_eval, v_hr_eval, v_bo_eval, v_bt_eval, v_rr_eval,
                 v_risk_factors, v_recommendations, NOW()
             );
END //

DELIMITER ;

