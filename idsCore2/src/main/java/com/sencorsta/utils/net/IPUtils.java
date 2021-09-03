package com.sencorsta.utils.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class IPUtils {
    /***
     * 测试主机Host的port端口是否被使用
     *
     * @param host
     * @param port
     * @throws UnknownHostException
     */
    public static boolean isPortUsing(String host, int port) throws UnknownHostException {
        boolean flag = false;
        InetAddress Address = InetAddress.getByName(host);
        Socket socket = null;
        try {
            socket = new Socket(Address, port); // 建立一个Socket连接
            flag = true;
        } catch (IOException e) {

        } finally {
            try {
                if (socket != null) {
                    socket.shutdownOutput();
                    socket.shutdownInput();
                    socket.close();
                }
            } catch (IOException e) {
            }
        }
        return flag;
    }
}
