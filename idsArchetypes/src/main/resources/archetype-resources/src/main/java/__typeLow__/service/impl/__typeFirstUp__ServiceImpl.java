package ${package}.${typeLow}.service.impl;


import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Service;
import ${package}.${typeLow}.dao.${typeFirstUp}Dao;
import ${package}.${typeLow}.service.${typeFirstUp}Service;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.sun.javafx.binding.StringFormatter;

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
        String res = StringFormatter.format("你好啊{}!!!我是{}", ${typeFirstLow}Dao.getName(s), "${typeFirstLow}").toString();
        log.info(res);
        return res;
    }
}
