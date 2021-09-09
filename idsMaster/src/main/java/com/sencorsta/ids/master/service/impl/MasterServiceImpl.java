package com.sencorsta.ids.master.service.impl;


import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Service;
import com.sencorsta.ids.master.dao.MasterDao;
import com.sencorsta.ids.master.service.MasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author daibin
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MasterServiceImpl implements MasterService {

    private final MasterDao masterDao;

    @Override
    public String helloWorld(String s) {
        String name = masterDao.getName(s);
        log.info("你好啊!!!" + name);
        return name;
    }
}
