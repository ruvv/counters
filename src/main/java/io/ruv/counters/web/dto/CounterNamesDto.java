package io.ruv.counters.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CounterNamesDto {

    /**
     * Counter names list
     */
    private List<String> names;


    public CounterNamesDto(List<String> names) {

        _setNames(names);
    }

    public void setNames(List<String> names) {

        _setNames(names);
    }

    private void _setNames(List<String> names) {

        this.names = List.copyOf(names);
    }
}
