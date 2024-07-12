package org.example.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MyCustumException extends Exception{
    public MyCustumException(String msg){
        super(msg);
    }
    public MyCustumException(){
        super("Something Went Wrong.");
    }

    public String getMsgObj(){
//        Msg msg = new Msg(this.getMessage());
//        try{
//            var mapper = new ObjectMapper();
//            return mapper.writeValueAsString(msg);
//        }catch (Exception q){
            return "{\"message\":\""+this.getMessage()+"\"}";
//        }
    }

    public class Msg {
        private String message;

        public Msg(String message) {
            this.message = message;
        }

        public String getMessage() {
            return this.message;
        }
    }
}
