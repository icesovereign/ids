package com.sencorsta.ids.master.dao;

import cn.hutool.core.util.RandomUtil;
import com.sencorsta.ids.core.entity.annotation.Repository;

/**
 * @author ICe
 */
@Repository
public class MasterDao {

    public String getName(String id) {
        return RandomUtil.randomString(id, 10);
    }
}
