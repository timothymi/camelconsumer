package com.dhi.camelkafka.controller;

import com.dhi.camelkafka.model.KafkaMessage;
import com.dhi.camelkafka.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for the Kafka message dashboard.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Display the main dashboard page.
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        return "dashboard";
    }

    /**
     * REST endpoint to get recent messages as JSON.
     */
    @GetMapping("/api/messages")
    @ResponseBody
    public ResponseEntity<List<KafkaMessage>> getRecentMessages() {
        List<KafkaMessage> messages = dashboardService.getRecentMessages();
        return ResponseEntity.ok(messages);
    }

    /**
     * REST endpoint to get messages for a specific topic.
     */
    @GetMapping("/api/messages/topic/{topic}")
    @ResponseBody
    public ResponseEntity<List<KafkaMessage>> getMessagesByTopic(@PathVariable String topic) {
        List<KafkaMessage> messages = dashboardService.getMessagesByTopic(topic);
        return ResponseEntity.ok(messages);
    }

    /**
     * REST endpoint to get dashboard statistics.
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<DashboardService.DashboardStats> getStats() {
        DashboardService.DashboardStats stats = dashboardService.getStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * REST endpoint to clear all messages.
     */
    @DeleteMapping("/api/messages")
    @ResponseBody
    public ResponseEntity<String> clearMessages() {
        dashboardService.clearMessages();
        return ResponseEntity.ok("Messages cleared successfully");
    }

    /**
     * Redirect root URL to dashboard.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
