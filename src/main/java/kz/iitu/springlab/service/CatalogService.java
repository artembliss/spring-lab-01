package kz.iitu.springlab.service;

import java.util.List;
import java.util.stream.IntStream;

import kz.iitu.springlab.audit.Audited;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {
    private final ObjectProvider<CatalogService> selfProvider;

    public CatalogService(ObjectProvider<CatalogService> selfProvider) {
        this.selfProvider = selfProvider;
    }

    public String findById(long id) {
        sleep(50);
        return "Item no. " + id;
    }

    @Audited(action = "CATALOG_LIST", logArguments = true)
    public List<String> findAll(int limit) {
        sleep(300);
        return IntStream.rangeClosed(1, limit)
                .mapToObj(i -> "Item no. " + i)
                .toList();
    }

    @Audited(action = "CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
        return "Removed item no. " + id;
    }

    public String removeTwiceDirect(long id) {
        String first = this.remove(id);
        String second = this.remove(id + 1);
        return first + "; " + second;
    }

    public String removeTwice(long id) {
        CatalogService self = selfProvider.getObject();
        String first = self.remove(id);
        String second = self.remove(id + 1);
        return first + "; " + second;
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
