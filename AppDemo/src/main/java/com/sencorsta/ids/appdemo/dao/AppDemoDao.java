package com.sencorsta.ids.appdemo.dao;

import cn.hutool.core.util.RandomUtil;
import com.sencorsta.ids.core.entity.annotation.Repository;

/**
 * @author ICe
 */
@Repository
public class AppDemoDao {

    public String getName(String id) {
        return RandomUtil.randomString(id, 10);
    }
}
