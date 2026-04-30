package com.gym.gymtracker.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardingController {
    @RequestMapping(value = {
        "/",
        "/categories",
        "/exercises",
        "/users",
        "/workouts",
        "/workouts/{workoutId}",
        "/sets"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
