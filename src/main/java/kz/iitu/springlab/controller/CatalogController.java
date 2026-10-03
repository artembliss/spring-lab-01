package kz.iitu.springlab.controller;

import java.util.List;
import java.util.Map;

import kz.iitu.springlab.aspect.CallCounterAspect;
import kz.iitu.springlab.service.CatalogService;
import org.springframework.aop.support.AopUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lab4")
public class CatalogController {
    private final CatalogService catalogService;
    private final CallCounterAspect callCounterAspect;

    public CatalogController(CatalogService catalogService,
                             CallCounterAspect callCounterAspect) {
        this.catalogService = catalogService;
        this.callCounterAspect = callCounterAspect;
    }

    @GetMapping("/item/{id}")
    public String findById(@PathVariable long id) {
        return catalogService.findById(id);
    }

    @GetMapping("/items")
    public List<String> findAll(@RequestParam(defaultValue = "5") int limit) {
        return catalogService.findAll(limit);
    }

    @DeleteMapping("/item/{id}")
    public String remove(@PathVariable long id) {
        return catalogService.remove(id);
    }

    @GetMapping("/proxy")
    public Map<String, String> proxyInfo() {
        return Map.of(
                "className", catalogService.getClass().getName(),
                "superClass", catalogService.getClass().getSuperclass().getSimpleName(),
                "isAopProxy", String.valueOf(AopUtils.isAopProxy(catalogService)),
                "isCglib", String.valueOf(AopUtils.isCglibProxy(catalogService)));
    }

    @GetMapping("/remove-twice-direct/{id}")
    public String removeTwiceDirect(@PathVariable long id) {
        return catalogService.removeTwiceDirect(id);
    }

    @GetMapping("/remove-twice/{id}")
    public String removeTwice(@PathVariable long id) {
        return catalogService.removeTwice(id);
    }

    @GetMapping("/stats")
    public Map<String, Long> statistics() {
        return callCounterAspect.statistics();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidIdentifier(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
