package ${package}.${typeLow}.service.impl;


import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Service;
import ${package}.${typeLow}.dao.${typeFirstUp}Dao;
import ${package}.${typeLow}.service.${typeFirstUp}Service;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ICe
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ${typeFirstUp}ServiceImpl implements ${typeFirstUp}Service {

    private final ${typeFirstUp}Dao ${typeFirstLow}Dao;

    @Override
    public String helloWorld(String s,Channel channel) {
        String name = ${typeFirstLow}Dao.getName(s);
        log.info("你好啊!!!" + name);
        return name;
    }
}
