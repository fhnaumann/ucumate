package me.fhnau.org.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Felix Naumann
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {


        //registry.addViewController("/tool/{spring:\\w+}")
        //        .setViewName("forward:/tool/index.html");
        //registry.addViewController("/tool/**/{spring:\\w+}")
        //        .setViewName("forward:/tool/index.html");

        registry.addViewController("/doc")
                .setViewName("forward:/doc/index.html");
        registry.addViewController("/doc/**")
                .setViewName("forward:/doc/index.html");

    }
}
