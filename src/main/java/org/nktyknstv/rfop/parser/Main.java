package org.nktyknstv.rfop.parser;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EntityScan("org.nktyknstv.rfop.parser.entity")
@EnableJpaRepositories(basePackages = "org.nktyknstv.rfop.parser.repository")
@EnableTransactionManagement
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
        SpringApplication.run(Main.class, args);
    }
}
