package com.micro.common.response;

import com.micro.common.code.BusinessCode;
import lombok.Builder;
import lombok.Data;

/**
 * user rest 接口返回包装vo
 *
 * @author Mr.zxb
 * @date 2020-06-04 21:40:59
 */
@Data
@Builder
public class ResultVO<T> {

    private Integer code;
    private String msg;
    private T data;

    public static ResultVO success() {
        return ResultVO.builder()
                .code(BusinessCode.OK.getCode())
                .msg(BusinessCode.OK.getMessage())
                .build();
    }

    public static <T> ResultVO<T> success(T data) {
        return ResultVO.<T>builder()
                .code(BusinessCode.OK.getCode())
                .msg(BusinessCode.OK.getMessage())
                .data(data)
                .build();
    }

    public static <T> ResultVO<T> fail(BusinessCode code, T data) {
        return ResultVO.<T>builder()
                .code(code.getCode())
                .msg(code.getMessage())
                .data(data)
                .build();
    }

    public static ResultVO fail(Throwable throwable) {
        return ResultVO.builder()
                .code(BusinessCode.SYSTEM_ERROR.getCode())
                .msg(BusinessCode.SYSTEM_ERROR.getMessage())
                .data(throwable.getMessage())
                .build();
    }


}
