package pl.edu.pjwstk.synteza;

public class Record {
    private final String fileName;
    private final boolean numeric;
    private final String phrase;


    public Record(String fileName, boolean numeric, String phrase) {
        this.fileName = fileName;
        this.numeric = numeric;
        this.phrase = phrase;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isNumeric() {
        return numeric;
    }
    public String getPhrase() {
        return phrase;
    }
}
