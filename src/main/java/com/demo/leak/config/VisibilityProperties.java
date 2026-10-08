package com.demo.leak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.visibility")
public class VisibilityProperties {

    /**
     * false = 使用普通 boolean（存在可见性问题）
     * true  = 使用 volatile boolean（修复可见性）
     */
    private boolean useVolatile = false;

    public boolean isUseVolatile() {
        return useVolatile;
    }

    public void setUseVolatile(boolean useVolatile) {
        this.useVolatile = useVolatile;
    }
}
