package com.anpandi.controller;

import com.anpandi.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequestMapping("/")
@SessionAttributes("username")
public class LoginController {

    @Autowired
    LoginService service;
    String query;

    @GetMapping("/login")
    public String loginPage() {
        return "loginPage";
    }
    @GetMapping("/welcome")
    public String welcomePage(Model model) {
        model.addAttribute("welcomeMessage","My Portal");
        return "welcomePage";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username, @RequestParam("password") String password, Model model) {

        // Replace this with your actual authentication logic
        if (service.validateUser(username, password)) {


            model.addAttribute("welcomeMessage", "Welcome, " + username + "!");
            return "redirect:/welcome";
        } else {
            model.addAttribute("errorMessage", "Invalid username or password.");
            return "loginPage";
        }
    }

    @PostMapping("/welcome")
    public String submitData(@RequestBody DataRequest dataRequest, Model model) {
        query = dataRequest.getQuery();
        System.out.println(query);
        model.addAttribute("Query", query);
        model.addAttribute("welcomeMessage","My Portal");
        return "redirect:/welcome";
    }

    public static class DataRequest {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }


    @GetMapping("/stream")
    public SseEmitter streamData(Model model) {
        model.addAttribute("welcomeMessage","My Portal");
        System.out.println(query);
        SseEmitter emitter = new SseEmitter();
        service.streamData(emitter,query);
        return emitter;
    }


}