package com.jalindi.myweb;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

@SpringBootApplication
public class MywebApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(MywebApplication.class, args);
	}
}
