package kz.iitu.springlab.web;

import kz.iitu.springlab.notify.NotificationService;
import org.springframework.web.bind.annotation.*;
import kz.iitu.springlab.lifecycle.LifecycleDemo;
import java.util.Map;
import java.util.List;
import kz.iitu.springlab.scope.TicketOffice;
import kz.iitu.springlab.notify.Notifier;
import org.springframework.beans.factory.annotation.Qualifier;

@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {

    private final NotificationService notifications;
    private final LifecycleDemo lifecycle;
    private final TicketOffice ticketOffice;
    private final Notifier upper;
    public Lab2Controller(
            NotificationService notifications,
            LifecycleDemo lifecycle,
            TicketOffice ticketOffice,
            @Qualifier("upper") Notifier upper) {

        this.notifications = notifications;
        this.lifecycle = lifecycle;
        this.ticketOffice = ticketOffice;
        this.upper = upper;
    }

    @GetMapping("/notify")
    public Map<String, Object> notify(
            @RequestParam(defaultValue = "Hello") String text) {

        return Map.of(
                "primary", notifications.viaPrimary(text),
                "console", notifications.viaConsole(text),
                "all", notifications.viaAll(text),
                "beanNames", notifications.names()
        );
    }

    @GetMapping("/lifecycle")
    public List<String> lifecycle() {
        return lifecycle.events();
    }

    @GetMapping("/scopes")
    public Map<String, Object> scopes() {
        return ticketOffice.demo();
    }

    @GetMapping("/custom")
    public String custom(@RequestParam String text) {
        return upper.send(text);
    }
}