package com.healthy.config;

import com.healthy.interceptor.JwtTokenUserInterceptor;
import com.healthy.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JacksonObjectMapper jacksonObjectMapper;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor; // 注入JWT用户拦截器

    /**
     * 注册拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册拦截器...");

        // 用户端JWT拦截器配置
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")  // 拦截用户相关接口
                .addPathPatterns("/health/**") // 新增：拦截健康相关接口
                .addPathPatterns("/medicine/**")  // 新增
                .addPathPatterns("/reminder/**")
                .addPathPatterns("/user-medicine/**")   // 新增这一行
                .addPathPatterns("/family/**")   // 新增：保护所有家庭接口
                .addPathPatterns("/prescription/**")
                .addPathPatterns("/ai/**")
                .excludePathPatterns("/user/login") // 排除登录接口
                .excludePathPatterns("/user/test") // 排除测试接口
                .excludePathPatterns("/reminder/save");

        log.info("拦截器注册完成");
    }

    /**
     * 配置跨域支持
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("配置跨域支持...");

        // 允许所有路径、所有来源、所有方法
        registry.addMapping("/**")  // 所有接口
                .allowedOriginPatterns("*")  // 允许所有来源，包括file://
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的请求方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true)  // 允许携带凭证（如cookies）
                .maxAge(3600);  // 预检请求的缓存时间（秒）
    }

    /**
     * 设置静态资源映射
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 配置JSON转换器
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jacksonConverter = (MappingJackson2HttpMessageConverter) converter;
                jacksonConverter.setObjectMapper(jacksonObjectMapper);
            }
        }
    }

    /**
     * 配置Swagger
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.healthy.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("健康管家API文档")
                .description("健康管家后端接口文档")
                .version("1.0")
                .build();
    }
}