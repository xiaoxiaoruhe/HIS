package com.zeroone.star.project.exception;

import com.zeroone.star.project.enums.ResultStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.AllArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * <p>
 * 描述：全局异常捕获处理
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@RestControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {
    /**
     * 响应数据包装器
     */
    private EhResultWrapper wrapper;

    /**
     * 系统通用异常处理
     * @param e 异常数据对象
     * @return 返回异常信息回显数据
     */
    @ExceptionHandler(value = Exception.class)
    public Object exceptionHandler(Exception e) {
        if (e instanceof HttpMediaTypeException) {
            return wrapper.getResult(e.getMessage(), ResultStatus.CONTENT_TYPE_ERR);
        }
        return wrapper.getResult(e.getMessage(), ResultStatus.SERVER_ERROR);
    }

    /**
     * requestBody参数校验异常处理
     * @param e 异常数据对象
     * @return 返回异常信息回显数据
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class, BindException.class})
    public Object methodArgumentNotValidHandler(Exception e) {
        BindingResult bindingResult;
        if (e instanceof MethodArgumentNotValidException me) {
            //@RequestBody参数校验
            bindingResult = me.getBindingResult();
        } else {
            //@ModelAttribute参数校验
            bindingResult = ((BindException) e).getBindingResult();
        }
        FieldError fieldError = bindingResult.getFieldError();
        String data = "[" + fieldError.getField() + "]" + fieldError.getDefaultMessage();
        return wrapper.getResult(data, ResultStatus.PARAMS_INVALID);
    }

    /**
     * requestParam参数校验异常处理
     * @param e 异常数据对象
     * @return 返回异常信息回显数据
     */
    @ExceptionHandler(value = {ConstraintViolationException.class, MissingServletRequestParameterException.class})
    public Object constraintViolationHandler(Exception e) {
        String field = "";
        String msg = "";
        if (e instanceof ConstraintViolationException ce) {
            ConstraintViolation<?> constraint = ce.getConstraintViolations().stream().findFirst().orElse(null);
            if (constraint != null) {
                List<Path.Node> pathList = StreamSupport.stream(constraint.getPropertyPath().spliterator(), false).toList();
                field = pathList.get(pathList.size() - 1).getName();
                msg = constraint.getMessage();
            }
        } else {
            // 非JSR标准返回的异常，要自定义提示文本
            field = ((MissingServletRequestParameterException) e).getParameterName();
            msg = "参数值缺失";
        }
        return wrapper.getResult("[" + field + "]" + msg, ResultStatus.PARAMS_INVALID);
    }
}