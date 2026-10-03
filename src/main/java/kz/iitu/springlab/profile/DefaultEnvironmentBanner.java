package kz.iitu.springlab.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev & !prod")
public class DefaultEnvironmentBanner implements EnvironmentBanner {

    @Override
    public String describe() {
        return "no profile is active";
    }
}