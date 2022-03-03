package ${package}.${typeLow}.controller;

import cn.hutool.core.io.FileUtil;
import com.sencorsta.ids.api.request.HelloFileRequest;
import com.sencorsta.ids.api.request.HelloWorldRequest;
import com.sencorsta.ids.core.entity.HttpFileResponse;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.Server;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Controller;
import com.sencorsta.ids.core.entity.annotation.RequestMapping;
import com.sencorsta.ids.core.net.handle.ContentTypes;
import com.sencorsta.ids.core.net.protocol.MessageJsonFactory;
import com.sencorsta.ids.core.net.protocol.RpcMessage;
import com.sencorsta.ids.core.processor.MessageProcessor;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ${package}.${typeLow}.service.${typeFirstUp}Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    public IdsResponse<String> helloWorld(IdsRequest<HelloWorldRequest> request) {

        RpcMessage message = MessageJsonFactory.newPushMessage();
        message.setMethod("/${typeFirstLow}/helloWorld/push");
        message.setUserId(request.getUserId());
        message.setChannel(request.getChannel());
        message.setJsonData(new Server());
        MessageProcessor.push(message);

        return new IdsResponse<>(${typeFirstLow}Service.helloWorld(request.getData().getName(),request.getChannel()));
    }

    @RequestMapping("/helloFile")
    public HttpFileResponse<String> helloFile(IdsRequest<HelloFileRequest> request) throws Exception {
        HttpFileResponse<String> stringHttpResponse = new HttpFileResponse<>();
        stringHttpResponse.setContentType(ContentTypes.APPLICATION_OCTET_STREAM);

        SelfSignedCertificate ssc = new SelfSignedCertificate();
        File certificate = ssc.certificate();
        File key = ssc.privateKey();

        File file = new File("testFile.zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
        zos.putNextEntry(new ZipEntry(certificate.getName()));
        zos.write(FileUtil.readBytes(certificate));
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry(key.getName()));
        zos.write(FileUtil.readBytes(key));
        zos.closeEntry();
        zos.close();

        stringHttpResponse.setFileName(file.getPath());
        stringHttpResponse.setFilePath(file.getAbsolutePath());
        return stringHttpResponse;
    }

}
