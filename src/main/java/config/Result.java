package config;

public class Result<T> {
    //    static class status{
//        public static int NOT_FOUND = 1;
//        public static int ALREADY_EXIST = 2;
//        public static int WRONG_VALUES = 3;
//    }
    private String msg;
    private T obj;

    public Result(){}

    public Result(String msg, T obj) {
        this.msg = msg;
        this.obj = obj;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }
}
