package com.anpandi.service;

import com.anpandi.controller.LoginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executors;

@Service
public class LoginService {

    	public boolean validateUser(String userid, String password) {
        return userid.equalsIgnoreCase("admin")
                && password.equalsIgnoreCase("password");
    }
    public void streamData(SseEmitter emitter,String query) {

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    emitter.send(query+" " + i);
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
    }

}
