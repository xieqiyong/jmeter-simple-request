package com.hz.perfma.request;

import com.alibaba.fastjson.JSON;
import com.hz.perfma.request.constant.Config;
import okhttp3.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * @Description 处理回放请求
 * @Author liusu <xieqiyong66@gmail.com>
 * @Version v1.0.0
 * @Date 2021/8/23
 */
public class SimpleRequest extends AbstractJavaSamplerClient {
    private static transient Logger log = LoggerFactory.getLogger(SimpleRequest.class);

    private SampleResult sampleResult;

    private String uri;

    private String host;

    private String requestHeader;

    private String method;

    private String requestBody;

    private String protocol;

    private String isForm;

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult sampleResult = new SampleResult();
        sampleResult.setSampleLabel("流量回放器");
        sampleResult.sampleStart();
        Response response = null;
        try {
            if(Config.Method.GET.equals(this.method)){
                response = doGet();
            }
            if(Config.Method.POST.equals(this.method)){
                response = doPost();
            }
            log.info("响应结果：{}", response.body());
            sampleResult.setSamplerData(this.requestBody);
            sampleResult.setRequestHeaders(this.requestHeader);
            sampleResult.setResponseData(response.body().string(), "utf-8");
            sampleResult.setResponseHeaders(response.headers().toString());
            sampleResult.setDataType("text");
            sampleResult.setSuccessful(true);
        } catch (Exception e) {
            sampleResult.setSuccessful(false);
        } finally {
            sampleResult.sampleEnd();
        }
        return sampleResult;
    }

    /**
     * 构造请求参数
     * <p>1.POST 2.GET 3.FILE</p>
     * @return {@link Arguments}
     */
    @Override
    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        //请求地址
        params.addArgument("host","");
        //请求路由
        params.addArgument("uri", "");
        //请求方式
        params.addArgument("method", "");
        //请求头
        params.addArgument("requestHeader", "");
        //请求体
        params.addArgument("requestBody", "");
        //协议
        params.addArgument("protocol", "");
        //post是否表单
        params.addArgument("isForm", "");
        return params;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        this.host = context.getParameter("host");
        this.uri = context.getParameter("uri");
        this.requestHeader = context.getParameter("requestHeader");
        this.method = context.getParameter("method");
        this.requestBody = context.getParameter("requestBody");
        this.isForm = context.getParameter("isForm");
        this.protocol = context.getParameter("protocol");
    }

    private Response doPost(){
        Headers headers = setHeaders(this.requestHeader);
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = this.protocol + this.host + this.uri;
        Request.Builder request = new Request.Builder().url(url);
        try {
            if(Config.Form.FORM.equals(this.isForm)){
                FormBody.Builder body = new FormBody.Builder();
                if(StringUtils.isNotBlank(this.requestBody)){
                    Map params = JSON.parseObject(this.requestBody, Map.class);
                    params.forEach((k,v) ->{
                        body.add(k.toString(),v.toString());
                    });
                }
                FormBody f = body.build();
                request.post(f);
            }
            if(Config.Form.BODY.equals(this.isForm)){
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(this.requestBody, mediaType);
                request.post(body);
            }
            if(ObjectUtils.isNotEmpty(headers)){
                request.headers(headers);
            }
            Request r = request.build();
            Call call = okHttpClient.newCall(r);
            return call.execute();
        }catch (Exception e){
            log.error("出错了：{}", e);
            return null;
        }
    }

    private Response doGet(){
        OkHttpClient okHttpClient = new OkHttpClient();
        Headers headers = setHeaders(this.requestHeader);
        String url = this.protocol + this.host + this.uri;
        Request.Builder request = new Request.Builder().url(url).get();
        if(ObjectUtils.isNotEmpty(headers)){
            request.headers(headers);
        }
        Request r = request.build();
        Call call = okHttpClient.newCall(r);
        try{
            return call.execute();
        }catch (Exception e){
            log.error("出错了：{}", e);
            return null;
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }

    public static Headers setHeaders(String headersParams) {
        Map<String, String> headerInfo = (Map<String, String>) JSON.parse(headersParams);
        Headers headers = null;
        if(StringUtils.isBlank(headersParams)){
            return null;
        }
        Headers.Builder headersBuilder = new Headers.Builder();
        Iterator<String> iterator = headerInfo.keySet().iterator();
        String key = "";
        while (iterator.hasNext()) {
            key = iterator.next();
            headersBuilder.add(key, headerInfo.get(key));
        }
        headers = headersBuilder.build();
        return headers;
    }
}
