package com.yanchen.exercise.thrift.server;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by yuyanchen on 16/10/20.
 */
@Controller
@RequestMapping(value = "/thrift")
public class HelloServiceDemo {

    @RequestMapping(value = "/serverStart")
    @ResponseBody
    public void startServer() {
        System.out.println("TSimpleServer start ...");
        com.yanchen.exercise.thrift.server.HelloWorldService.Processor<com.yanchen.exercise.thrift.server.HelloWorldService.Iface> processor = new com.yanchen.exercise.thrift.server.HelloWorldService.Processor<>(new HelloWorldImpl());

        try {
            TServerSocket tServerSocket = new TServerSocket(8090);
            TServer.Args tArgs = new TServer.Args(tServerSocket);
            tArgs.processor(processor);
            tArgs.protocolFactory(new TBinaryProtocol.Factory());
            TSimpleServer server = new TSimpleServer(tArgs);
            server.serve();
        } catch (TTransportException e) {
            System.out.println("TSimpleServer start error !!!");
            e.printStackTrace();
        }
    }
}
