package kz.iitu.springlab.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProdEnvironmentBanner implements EnvironmentBanner {

    @Override
    public String describe() {
        return "PRODUCTION: handle with care";
    }
}