package pl.edu.pjwstk.synteza;

import org.apache.commons.lang3.StringUtils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        Set<Platform> platforms = Set.of(
                new Platform(Set.of("siodmego", "siodmym", "piatego", "piatym"), Set.of("pierwszym", "pierwszego")),
                new Platform(Set.of("trzecim", "trzeciego", "pierwszym", "pierwszego"), Set.of("drugim", "drugiego")),
                new Platform(Set.of("drugim", "drugiego", "czwartym", "czwartego"), Set.of("trzecim", "trzeciego")),
                new Platform(Set.of("szostym", "szóstego", "osmego", "osmym"), Set.of("czwartym", "czwartego"))
        );

        Map<String, Record> records = new HashMap<>();
        addRecorde("stacje", false, records);
        addRecorde("perony_i_tory", false, records);
        addRecorde("do_z_stacji", false, records);
        addRecorde("numbers", true, records);

        if (args.length == 0) {
            throw new IllegalArgumentException("No input text provided");
        }
        String[] tokens = tokenize(args);

        List<Record> synthesized = synthesize(tokens, records);

        validatePlatforms(synthesized, platforms);
        generateAudio(synthesized);
    }

    private static String[] tokenize(String[] args) {
        String inputText = args[0];
        String normalizedText = StringUtils.stripAccents(inputText);
        String[] tokens = normalizedText.toLowerCase()
                .replaceAll("[,.]", "")
                .split(" ");
        return tokens;
    }

    private static List<Record> synthesize(String[] tokens, Map<String, Record> records) {
        List<Record> synthesized = new ArrayList<>();
        String notMatched = "";
        for (String token : tokens) {
            String phrase = notMatched + token;
            if (records.containsKey(phrase)) {
                synthesized.add(records.get(phrase));
                notMatched = "";
            } else {
                notMatched += token + " ";
            }
        }

        if (!notMatched.isEmpty()) {
            throw new IllegalArgumentException("Missing record for phrase: " + notMatched);
        }
        return synthesized;
    }

    private static void generateAudio(List<Record> syntesed) throws UnsupportedAudioFileException, IOException {
        List<AudioInputStream> clips = new ArrayList<>();
        for (Record record : syntesed) {
            clips.add(AudioSystem.getAudioInputStream(new File(record.getFileName())));
        }
        long frameLength = clips.stream().map(AudioInputStream::getFrameLength).reduce(0L, Long::sum);
        AudioInputStream appendedFiles =
                new AudioInputStream(
                        new SequenceInputStream(Collections.enumeration(clips)),
                        clips.get(0).getFormat(), frameLength);

        AudioSystem.write(appendedFiles,
                AudioFileFormat.Type.WAVE,
                new File("output.wav"));

        for (AudioInputStream clip : clips) {
            clip.close();
        }
    }

    private static void validatePlatforms(List<Record> syntesed, Set<Platform> platforms) {
        Record track = null;
        Record platform = null;
        for (Record s : syntesed) {
            if (s.isNumeric()) {
                if (track == null) {
                    track = s;
                } else {
                    platform = s;
                }
            }
        }

        for (Platform p : platforms) {
            if (p.isMatching(platform.getPhrase())) {
                if (!p.hasTrack(track.getPhrase())) {
                    throw new IllegalArgumentException("Invalid track number for platform: " + track.getPhrase() + " " + platform.getPhrase());
                }
                break;
            }
        }
    }

    public static void addRecorde(String directory, boolean numeric, Map<String, Record> records) throws IOException {
        Files.list(Paths.get("nagrania/" + directory))
                .filter(path -> path.toString().endsWith(".wav"))
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    String phrase = fileName.replace("_", " ").replace(".wav", "");
                    records.put(phrase, new Record(path.toAbsolutePath().toString(), numeric, phrase));
                });
    }
}

