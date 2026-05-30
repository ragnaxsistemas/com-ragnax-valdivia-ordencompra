package com.ragnax.valdivia.ordencompra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ComRagnaxValdiviaOrdencompraApplication implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("---Starting Ragnax Valdivia Ordencompra Application---");
    }

	public static void main(String[] args) {
        SpringApplication.run(ComRagnaxValdiviaOrdencompraApplication.class, args);
	}

}
