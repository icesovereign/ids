package ${package}.${typeLow}.controller;

import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Controller;
import com.sencorsta.ids.core.entity.annotation.RequestMapping;
import com.sencorsta.ids.core.net.protocol.MessageJsonFactory;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import ${package}.${typeLow}.service.${typeFirstUp}Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ICe
 */
@Controller
@RequestMapping("/${typeFirstLow}")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ${typeFirstUp}Controller {

    private final ${typeFirstUp}Service ${typeFirstLow}Service;

    @RequestMapping("/helloWorld")
    public IdsResponse<String> helloWorld(IdsRequest<byte[]> request) {

        RpcMessage message = MessageJsonFactory.newPushMessage();
        message.setMethod("/${typeFirstLow}/helloWorld/push");
        message.setUserId(request.getUserId());
        message.setChannel(request.getChannel());
        message.setJsonData(new Server());
        MessageProcessor.push(message);

        return new IdsResponse<>(${typeFirstLow}Service.helloWorld(new String(request.getData()),request.getChannel()));
    }

}
