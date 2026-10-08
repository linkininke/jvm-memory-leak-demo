package com.demo.leak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.parse")
public class AppProperties {

    private int rowsPerUpload = 5000;
    private int payloadSizeKb = 64;

    public int getRowsPerUpload() {
        return rowsPerUpload;
    }

    public void setRowsPerUpload(int rowsPerUpload) {
        this.rowsPerUpload = rowsPerUpload;
    }

    public int getPayloadSizeKb() {
        return payloadSizeKb;
    }

    public void setPayloadSizeKb(int payloadSizeKb) {
        this.payloadSizeKb = payloadSizeKb;
    }
}
