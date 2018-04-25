package rs.ac.bg.etf.opj;

public class Line {
    private static final String SEPARATOR = "\t";


    private boolean isAnotated;
    private int score;
    private String id;
    private String comment;

    public Line(String lineContent) {
        String[] lineParts = lineContent.trim().split(SEPARATOR);

        if (lineParts.length < 2) {
            throw new LineParsingException("Line has less than 2 parts. Line content: " + lineContent);
        }

        if (lineParts.length > 3) {
            throw new LineParsingException("Line has more than 3 parts. Line content: " + lineContent);
        }

        isAnotated = lineParts.length == 3;

        if (isAnotated) {
            score = Integer.parseInt(lineParts[0]);
            id = lineParts[1];
            comment = lineParts[2];
        } else {
            id = lineParts[0];
            comment = lineParts[1];
        }
    }

    public boolean isAnotated() {
        return isAnotated;
    }

    public void setScore(int score) {
        this.score = score;
        isAnotated = true;
    }

    public int getScore() {
        return score;
    }

    public String getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        String result = id + "\t" + comment;

        if (isAnotated) {
            result = score + "\t" + result;
        }

        return result;
    }
}
