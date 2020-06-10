package com.micro.webim.configuration;

import com.micro.common.code.BusinessCode;
import com.micro.common.response.ResultVO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Mr.zxb
 * @date 2020-06-10 21:02:21
 */
@RestControllerAdvice
public class BaseController {

    @ExceptionHandler(value = Exception.class)
    public ResultVO<String> error() {
        return ResultVO.fail(BusinessCode.SYSTEM_ERROR);
    }
}
