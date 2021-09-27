package com.sencorsta.ids.idsgateway.dao;

import cn.hutool.core.util.RandomUtil;
import com.sencorsta.ids.core.entity.annotation.Repository;

/**
 * @author ICe
 */
@Repository
public class IdsGatewayDao {

    public String getName(String id) {
        return RandomUtil.randomString(id, 10);
    }
}
