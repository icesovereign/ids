package com.sencorsta.ids.appdemo.service.impl;


import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Service;
import com.sencorsta.ids.appdemo.dao.AppDemoDao;
import com.sencorsta.ids.appdemo.service.AppDemoService;
import com.sun.javafx.binding.StringFormatter;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ICe
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AppDemoServiceImpl implements AppDemoService {

    private final AppDemoDao appDemoDao;

    @Override
    public String helloWorld(String s,Channel channel) {
        String res = StringFormatter.format("你好啊{}!!!我是{}", appDemoDao.getName(s), "appDemo").toString();
        log.info(res);
        return res;
    }
}
