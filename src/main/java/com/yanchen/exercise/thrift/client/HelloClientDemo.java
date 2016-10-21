package com.yanchen.exercise.thrift.client;

import com.yanchen.exercise.test.TestService;
import com.yanchen.exercise.thrift.server.HelloWorldService;
import org.apache.ibatis.annotations.Param;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by yuyanchen on 16/10/20.
 */
@Controller
@RequestMapping(value = "/thrift")
public class HelloClientDemo {

    @Resource
    private TestService testService;

    @Value("${thrift.ip}")
    private String IP;

    @Value("${thrift.port}")
    private Integer PORT;

    @Value("${thrift.timeout}")
    private Integer TIMEOUT;

    @RequestMapping(value = "/test")
    @ResponseBody
    public String startClient(@Param("userName") String userName) {
        TSocket socket = new TSocket(IP, PORT, TIMEOUT);
        try {
            TBinaryProtocol protocol = new TBinaryProtocol(socket);
            HelloWorldService.Client client = new HelloWorldService.Client(protocol);
            socket.open();

            //调用
            String result = client.sayHello(userName);
            System.out.println("thrift client result:" + result);
            return result;
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                socket.close();
            }
        }

        return "OK";
    }

}
