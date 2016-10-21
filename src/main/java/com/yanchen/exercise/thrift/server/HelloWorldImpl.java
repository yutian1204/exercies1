package com.yanchen.exercise.thrift.server;

/**
 * Created by yuyanchen on 16/10/20.
 */
public class HelloWorldImpl implements com.yanchen.exercise.thrift.server.HelloWorldService.Iface {
    @Override
    public String sayHello(String username) throws org.apache.thrift.TException {
        return "hello," + username;
    }
}
