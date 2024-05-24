package pl.edu.pjwstk.synteza;

import java.util.Set;

public class Platform {
    private final Set<String> trackNumbers;
    private final Set<String> values;

    public Platform(Set<String> trackNumbers, Set<String> values) {
        this.trackNumbers = trackNumbers;
        this.values = values;
    }

    public boolean isMatching(String value) {
        return values.contains(value);
    }

    public boolean hasTrack(String trackNumber) {
        return trackNumbers.contains(trackNumber);
    }
}

