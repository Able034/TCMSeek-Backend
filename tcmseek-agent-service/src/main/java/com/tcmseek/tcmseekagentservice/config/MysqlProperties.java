package com.tcmseek.tcmseekagentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public class MysqlProperties {
    private String driverClassName;

    private String url;

    private String username;

    private String password;

    private Hikari hikari = new Hikari();

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Hikari getHikari() {
        return hikari;
    }

    public void setHikari(Hikari hikari) {
        this.hikari = hikari;
    }

    public static class Hikari {
        private Integer maximumPoolSize = 5;

        private Integer minimumIdle = 1;

        private Long connectionTimeout = 5000L;

        private Long initializationFailTimeout = -1L;

        public Integer getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(Integer maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public Integer getMinimumIdle() {
            return minimumIdle;
        }

        public void setMinimumIdle(Integer minimumIdle) {
            this.minimumIdle = minimumIdle;
        }

        public Long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public Long getInitializationFailTimeout() {
            return initializationFailTimeout;
        }

        public void setInitializationFailTimeout(Long initializationFailTimeout) {
            this.initializationFailTimeout = initializationFailTimeout;
        }
    }
}
