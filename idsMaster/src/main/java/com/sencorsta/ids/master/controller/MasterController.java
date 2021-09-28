package com.sencorsta.ids.master.controller;

import cn.hutool.core.io.FileUtil;
import com.sencorsta.ids.api.request.HelloFileRequest;
import com.sencorsta.ids.api.request.HelloWorldRequest;
import com.sencorsta.ids.core.application.master.request.GetTotalServerRequest;
import com.sencorsta.ids.core.application.master.request.JoinMasterRequest;
import com.sencorsta.ids.core.application.master.request.PingMasterRequest;
import com.sencorsta.ids.core.application.master.response.GetTotalServerResponse;
import com.sencorsta.ids.core.application.master.response.JoinMasterResponse;
import com.sencorsta.ids.core.application.master.response.PingMasterResponse;
import com.sencorsta.ids.core.entity.ErrorCode;
import com.sencorsta.ids.core.entity.HttpFileResponse;
import com.sencorsta.ids.core.entity.IdsRequest;
import com.sencorsta.ids.core.entity.IdsResponse;
import com.sencorsta.ids.core.entity.annotation.Autowired;
import com.sencorsta.ids.core.entity.annotation.Controller;
import com.sencorsta.ids.core.entity.annotation.RequestMapping;
import com.sencorsta.ids.core.net.handle.ContentTypes;
import com.sencorsta.ids.master.service.MasterService;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.cert.CertificateException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author ICe
 */
@Controller
@RequestMapping("/master")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MasterController {

    private final MasterService masterService;

    @RequestMapping("/helloWorld")
    public IdsResponse<String> helloWorld(IdsRequest<HelloWorldRequest> request) {
        return new IdsResponse<>(masterService.helloWorld(request.getData().getName()));
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


    @RequestMapping("/sleep")
    public IdsResponse<String> sleep(IdsRequest<byte[]> request) {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new IdsResponse<>(masterService.helloWorld(new String(request.getData())));
    }

    @RequestMapping("/pingMaster")
    public IdsResponse<PingMasterResponse> pingMaster(IdsRequest<PingMasterRequest> request) throws ErrorCode {
        return masterService.pingMaster(request.getData(), request.getChannel());
    }

    @RequestMapping("/joinMaster")
    public IdsResponse<JoinMasterResponse> joinMaster(IdsRequest<JoinMasterRequest> request) throws ErrorCode {
        return masterService.joinMaster(request.getData(), request.getChannel());
    }

    @RequestMapping("/getTotalServer")
    public IdsResponse<GetTotalServerResponse> getTotalServer(IdsRequest<GetTotalServerRequest> request) {
        return masterService.getTotalServer(request.getData());
    }
}
