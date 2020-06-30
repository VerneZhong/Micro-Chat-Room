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
public class ResultPageVO<T> {

    private Integer code;
    private String msg;
    private T data;
    private Integer pages;
    private Long memberIdx;

    public static ResultPageVO success() {
        return ResultPageVO.builder()
                .code(BusinessCode.OK.getCode())
                .msg(BusinessCode.OK.getMessage())
                .build();
    }

    public static <T> ResultPageVO<T> success(T data, Integer pages, Long memberIdx) {
        return ResultPageVO.<T>builder()
                .code(BusinessCode.OK.getCode())
                .msg(BusinessCode.OK.getMessage())
                .data(data)
                .pages(pages)
                .memberIdx(memberIdx)
                .build();
    }
}
