package com.epam.edp.demo.dto;

import java.util.ArrayList;
import java.util.List;

public class DestinationListResponseDTO {

    private List<String> destinations;

    public DestinationListResponseDTO() {}

    public DestinationListResponseDTO(List<String> destinations) {
        this.destinations = copyOf(destinations);
    }

    public List<String> getDestinations() {
        return copyOf(destinations);
    }

    public void setDestinations(List<String> destinations) {
        this.destinations = copyOf(destinations);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<String> destinations;

        public Builder destinations(List<String> destinations) {
            this.destinations = copyOf(destinations);
            return this;
        }

        public DestinationListResponseDTO build() {
            return new DestinationListResponseDTO(destinations);
        }
    }

    private static List<String> copyOf(List<String> destinations) {
        return destinations == null ? null : new ArrayList<>(destinations);
    }
}