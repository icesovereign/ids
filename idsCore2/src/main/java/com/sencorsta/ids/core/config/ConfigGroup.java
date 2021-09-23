package com.sencorsta.ids.core.config;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配置的分类枚举
 *
 * @author ICe
 */
@AllArgsConstructor
public enum ConfigGroup {
    /**
     * 核心配置
     */
    core("core"),
    /**
     * 性能参数
     */
    performance("performance"),
    /**
     * 服务器配置
     */
    server("server"),
    /**
     * master服务治理
     */
    master("master"),
    /**
     * 加载额外配置
     */
    include("include"),
    /**
     * mysql配置
     */
    mysql("mysql"),
    /**
     * redis配置
     */
    redis("redis"),
    ;
    @Getter
    String name;
}
