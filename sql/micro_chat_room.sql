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
  `avatar_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '头像',
  `age` int(3) DEFAULT NULL COMMENT '年龄',
  `area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地区',
  `is_locked` int(2) NOT NULL DEFAULT '0' COMMENT '0未锁定，1锁定',
  `register_date` date NOT NULL COMMENT '注册日期',
  `sign` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '签名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `nickname_index` (`nickname`(191)) USING BTREE COMMENT '账号唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT;

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
