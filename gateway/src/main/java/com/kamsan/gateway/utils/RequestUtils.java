package com.kamsan.gateway.utils;

import com.kamsan.gateway.sharedkernel.exception.ApiException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import tools.jackson.databind.ObjectMapper;

import java.util.function.BiConsumer;

public class RequestUtils {
    private static final BiConsumer<HttpServletResponse, Response> writeResponse = (servletResponse, response) -> {
        try {
            ServletOutputStream outputStream = servletResponse.getOutputStream();
            new ObjectMapper().writeValue(outputStream, response);
        } catch (Exception ex) {
            throw new ApiException(ex.getMessage());
        }
    };
}
