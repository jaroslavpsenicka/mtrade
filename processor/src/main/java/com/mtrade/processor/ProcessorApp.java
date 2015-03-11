//
// Copyright (c) 2011-2014 Xanadu Consultancy Ltd.,
//

package com.mtrade.processor;

import java.io.IOException;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The application executable.
 */
@SpringBootApplication
public class ProcessorApp implements CommandLineRunner {

    public void run(String... args) throws IOException {
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run("classpath:/processor-context.xml", args);
    }
}