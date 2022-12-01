package io.ruv.counters.web.controller;

import io.ruv.counters.service.CountersService;
import io.ruv.counters.web.dto.CounterDto;
import io.ruv.counters.web.dto.CounterNamesDto;
import io.ruv.counters.web.dto.CounterSumDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/counters")
public class CountersController {

    private final CountersService service;

    @PostMapping
    public CounterDto create(@RequestBody CounterDto dto) {

        if (dto.getName() == null) {

            dto.setName(UUID.randomUUID().toString());
        }

        return service.create(dto);
    }

    @PutMapping("/{name}")
    public CounterDto insert(@PathVariable String name, @RequestBody CounterDto dto) {

        dto.setName(name);

        return service.create(dto);
    }

    @PostMapping("/{name}")
    public CounterDto increment(@PathVariable String name) {

        return service.incrementByName(name);
    }

    @GetMapping("{name}")
    public CounterDto get(@PathVariable String name) {

        return service.getByName(name);
    }

    @DeleteMapping("{name}")
    public CounterDto delete(@PathVariable String name) {

        return service.deleteByName(name);
    }

    @GetMapping("/extension/values-sum")
    public CounterSumDto valuesSum() {

        return service.getCounterSum();
    }

    @GetMapping("/extension/names-list")
    public CounterNamesDto counterNames() {

        return service.getCounterNames();
    }
}
