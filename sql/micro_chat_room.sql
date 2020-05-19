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

 Date: 19/05/2020 16:32:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `nickname` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '昵称',
  `account` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '帐号名称',
  `cellphone_number` char(11) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号码',
  `is_online` int(2) NOT NULL COMMENT '是否在线',
  `register_date` date NOT NULL COMMENT '注册日期',
  `avatar` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '头像',
  `avatar_thumbnail` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '头像缩略图',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
