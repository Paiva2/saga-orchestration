package org.com.sagapattern.orchestrator.application.entrypoint.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("orchestrator")
public class OrchestratorController {
    @GetMapping("/status")
    public String status() {
        return "OK";
    }
}
