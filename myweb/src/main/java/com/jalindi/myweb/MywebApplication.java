package com.jalindi.myweb;

import com.jalindi.includeweb.IncludewebApplication;
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
		new IncludewebApplication();
		InputStream stream1=IncludewebApplication.class.getResourceAsStream("/static/myhtml.html");
		InputStream stream2=IncludewebApplication.class.getResourceAsStream("/static/index.html");
		InputStream stream=IncludewebApplication.class.getClassLoader().getResourceAsStream("/static/myhtml.html");
		IOUtils.copy(stream1, System.out);
		SpringApplication.run(MywebApplication.class, args);
	}
}
