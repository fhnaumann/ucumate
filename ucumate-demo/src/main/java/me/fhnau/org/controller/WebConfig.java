package me.fhnau.org.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.util.List;

/**
 * @author Felix Naumann
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/doc", "/doc/");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/doc/**")
                    .addResourceLocations("classpath:/static/doc/")
                    .resourceChain(true)
                    .addResolver(new IndexFallbackResourceResolver());
    }

        static class IndexFallbackResourceResolver implements ResourceResolver {

            @Override
            public Resource resolveResource(HttpServletRequest request, String requestPath,
                                            List<? extends Resource> locations, ResourceResolverChain chain) {
                // Try to resolve normally
                Resource resource = chain.resolveResource(request, requestPath, locations);
                if (resource != null) return resource;

                // Try index.html inside the path (e.g., /doc/page1/ → /doc/page1/index.html)
                if (!requestPath.endsWith("/")) requestPath += "/";
                return chain.resolveResource(request, requestPath + "index.html", locations);
            }

            @Override
            public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                                         ResourceResolverChain chain) {
                String path = chain.resolveUrlPath(resourcePath, locations);
                if (path != null) return path;

                // Try index.html fallback
                if (!resourcePath.endsWith("/")) resourcePath += "/";
                return chain.resolveUrlPath(resourcePath + "index.html", locations);
            }
        }
}
