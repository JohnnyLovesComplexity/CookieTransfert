package fr.polytech.arar.cookietransfert;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

//RFC1350
public enum ErrorCode {

        NOT_DEFINED(0,"NOT_DEFINED"),
        FILE_NOT_FOUND(1,"FILE_NOT_FOUND"),
        ACCESS_VIOLATION(2,"ACCESS_VIOLATION"),
        ALLOCATION_EXCEEDED(3,"ALLOCATION_EXCEEDED"),
        ILLEGAL_TFTP_OPERATION(4,"ILLEGAL_TFTP_OPERATION"),
        UNKNOWN_TRANSFERT_ID(5,"UNKNOWN_TRANSFERT_ID"),
        FILE_ALREADY_EXISTS(6,"FILE_ALREADY_EXISTS"),
        NO_SUCH_USER(7,"NO_SUCH_USER");

        private int code;
        private String mess;

        ErrorCode(int code, String mess) {
            setCode(code);
            setMess(mess);
        }

        /* GETTER & SETTER */

        @Contract(pure = true)
        public int getCode() {
            return code;
        }

    public String getMess() {
        return mess;
    }

    private void setCode(int code) {
            this.code = code;
        }

    public void setMess(String mess) {
        this.mess = mess;
    }

    public static String errorMessage(int num){
            ErrorCode[] values = ErrorCode.values();
        for (ErrorCode value : values) {
            if (value.getCode() == num)
                return  value.getMess();
        }
          return "UNKNOWN_CODE";
    }
}
