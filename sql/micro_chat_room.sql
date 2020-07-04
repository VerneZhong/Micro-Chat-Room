/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80017
 Source Host           : localhost:3306
 Source Schema         : micro_chat_room

 Target Server Type    : MySQL
 Target Server Version : 80017
 File Encoding         : 65001

 Date: 11/06/2020 17:42:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for group
-- ----------------------------
DROP TABLE IF EXISTS `group`;
CREATE TABLE `group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '群名',
  `avatar` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '群头像',
  `create_date` date DEFAULT NULL COMMENT '创建日期',
  `creator` bigint(20) NOT NULL COMMENT '群创建者',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '帐号名称',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户密码',
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '昵称',
  `cellphone_number` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号码',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮箱地址',
  `avatar_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '头像',
  `age` int(3) DEFAULT NULL COMMENT '年龄',
  `area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地区',
  `is_locked` int(2) NOT NULL DEFAULT '0' COMMENT '0未锁定，1锁定',
  `register_date` date NOT NULL COMMENT '注册日期',
  `sign` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '签名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `nickname_index` (`nickname`(191)) USING BTREE COMMENT '账号唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT;

-- ----------------------------
-- Table structure for user_friends
-- ----------------------------
DROP TABLE IF EXISTS `user_friends`;
CREATE TABLE `user_friends` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `friend_id`  bigint(11)  NOT NULL COMMENT '账号',
  `user_id`  bigint(11)  NOT NULL COMMENT '好友账号',
  `group_id` bigint(11) NOT NULL COMMENT '好友分组id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT;

-- ----------------------------
-- Table structure for user_friends_group
-- ----------------------------
DROP TABLE IF EXISTS `user_friends_group`;
CREATE TABLE `user_friends_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分组主键id',
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '好友分组名称',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user_group_relation
-- ----------------------------
DROP TABLE IF EXISTS `user_group_relation`;
CREATE TABLE `user_group_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL COMMENT '群组id',
  `user_id` bigint(20) NOT NULL COMMENT '群员id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `type` tinyint(1) NOT NULL COMMENT '消息类型（1为请求添加用户消息）；2为系统消息（添加好友）；3为请求加群消息；4为系统消息（添加群系统消息）；5为全体用户消息',
  `form` bigint(20) NOT NULL COMMENT '消息发送者（0表示为系统消息）',
  `to` bigint(20) NOT NULL COMMENT '消息接收者（0位全体用户）',
  `status` tinyint(4) NOT NULL COMMENT '1未读，2同意，3拒绝，4同意且返回消息已读，5拒绝且返回消息已读，6全体消息已读',
  `remark` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '附加消息',
  `sendTime` date DEFAULT NULL COMMENT '发送消息时间',
  `readTime` date DEFAULT NULL COMMENT '读取消息时间',
  `adminGroup` bigint(20) DEFAULT NULL COMMENT '接收消息的管理员',
  `handler` bigint(20) DEFAULT NULL COMMENT '处理该请求的管理员ID',
  `friend_groupid` bigint(20) DEFAULT NULL COMMENT '好友分组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- update table field
ALTER TABLE `message`
    CHANGE COLUMN `friend_groupid` `friend_group_id`  bigint(20) NULL DEFAULT NULL COMMENT '好友分组' AFTER `handler`;

ALTER TABLE `message`
    MODIFY COLUMN `type`  tinyint(1) NOT NULL COMMENT '消息类型（1为请求添加用户消息）；2为系统消息（添加好友）；3为请求加群消息；4为系统消息（添加群系统消息）；5为全体用户消息' AFTER `id`;

-- 新增
ALTER TABLE `micro_chat_room`.`user_friends_group`
ADD COLUMN `type` bigint(1) NOT NULL COMMENT '分组类型（0默认分组，1是自定义分组）' AFTER `user_id`;

ALTER TABLE `micro_chat_room`.`user_friends`
ADD COLUMN `remark` varchar(255) NULL COMMENT '好友备注' AFTER `group_id`;
