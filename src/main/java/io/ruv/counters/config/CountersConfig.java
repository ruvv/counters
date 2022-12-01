package io.ruv.counters.config;

import io.ruv.counters.repo.CountersRepository;
import io.ruv.counters.repo.concurrentmap.ConcurrentLongCountersRepository;
import io.ruv.counters.service.CountersService;
import io.ruv.counters.service.impl.CountersServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CountersConfig {

    @Bean
    public CountersService countersService() {

        return new CountersServiceImpl(countersRepository());
    }

    @Bean
    public CountersRepository countersRepository() {

        return new ConcurrentLongCountersRepository();
    }
}
