package org.nktyknstv.rfop.parser;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

@SpringBootApplication
@EntityScan("org.nktyknstv.rfop.parser.entity")
@EnableJpaRepositories(basePackages = "org.nktyknstv.rfop.parser.repository")
@EnableTransactionManagement
@Slf4j
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello, world!");
        log.debug("Logger enabled");

        File source = new File("rfop-parser.db");
        String backupFileName = "rfop-parser.db." + LocalDateTime.now().toString().replaceAll("[\\s:\\-T]", "_") + ".backup";
        File dest = new File(backupFileName);
        log.info("Creating database backup..");
        Files.copy(source.toPath(), dest.toPath());
        log.info("Created database backup: {}", backupFileName);

        SpringApplication.run(Main.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowCredentials(true)
                        .allowedMethods("GET", "PUT", "POST", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }

}
