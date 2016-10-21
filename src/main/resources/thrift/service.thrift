namespace java com.yanchen.exercise.thrift.server

service HelloWorldService {
    string sayHello(1:string username)
}
