package com.batm.config;

import com.batm.model.Error;
import com.batm.model.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class FileUploadExceptionAdvice {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxUploadSize;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Response handleMaxSizeException(MaxUploadSizeExceededException exc) {
        exc.printStackTrace();
        return Response.error(new Error(2, "File should be up to " + maxUploadSize));
    }
}