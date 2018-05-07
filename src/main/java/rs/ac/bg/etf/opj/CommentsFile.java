package rs.ac.bg.etf.opj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommentsFile {
    private static final String SEPARATOR = "\n";

    private List<Line> lines;

    public CommentsFile(String fileContent) {
        lines = Arrays.stream(fileContent.split(SEPARATOR))
                .map(Line::new)
                .collect(Collectors.toList());
    }

    public void writeScore(int lineIndex, int score) {
        lines.get(lineIndex).setScore(score);
    }

    public int linesCount() {
        return lines.size();
    }

    public int annotatedCount() {
        return lines.stream().mapToInt(line -> line.isAnotated() ? 1 : 0).sum();
    }

    public int notAnnotatedCount() {
        return linesCount() - annotatedCount();
    }


    public List<Line> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public Line getLine(int index) {
        return lines.get(index);
    }
}
