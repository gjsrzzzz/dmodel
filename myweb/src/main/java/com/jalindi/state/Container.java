package com.jalindi.state;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@ToString
public class Container {
    private @NonNull final String repeatKey;
    private @NonNull final String serial;
    public Container(String repeatKey)
    {
        this.repeatKey=repeatKey;
        this.serial=createSerial();
    }

    private static int counter=0;
    private String createSerial() {
        long time = System.currentTimeMillis()*100+counter;
        counter++;
        String hex = Long.toHexString(time).toString().toUpperCase();
        StringBuffer builder=new StringBuffer();
        for (int i=0; i<10; i++)
        {
            char c=hex.charAt(hex.length()-1-i);
            builder.append(c);
        }
        return builder.toString();
    }
}
