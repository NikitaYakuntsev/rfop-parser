package org.nktyknstv.rfop.parser.service;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class DiffService {

    private Pattern WORDS_REGEX = Pattern.compile("([А-Яа-яЁё].*[А-Яа-яЁё])");


    public String calcDiff(String before, String after) {


        try {
            DiffRowGenerator generator = DiffRowGenerator.create()
                    .ignoreWhiteSpaces(true)
                    .showInlineDiffs(true)
//                    .mergeOriginalRevised(true)
                    .inlineDiffByWord(true)
                    .oldTag(s -> "")
                    .newTag(s -> s ? "<mark>" : "</mark>")
                    .build();
            List<DiffRow> diffRows = generator.generateDiffRows(
                    prepareInput(before),
                    prepareInput(after)
            );
            return diffRows.stream()
                    .map(DiffService::getChangeRow)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("<br>"));
//                    .replaceAll("&lt;", "<")
//                    .replaceAll("&gt;", ">");
        } catch (DiffException e) {
            return "";
        }
    }

    private String getChangeRow(DiffRow diffRow) {
        if (diffRow.getTag() == DiffRow.Tag.DELETE) {
            return getDeletedLineFormatted(diffRow.getOldLine());
        }
        return diffRow.getNewLine();
    }

    private String getDeletedLineFormatted(String oldLine) {
        Matcher oldMatcher = WORDS_REGEX.matcher(oldLine);
        if (oldMatcher.find()) {
            String russianTextOld = oldMatcher.group();
            int len = Math.min(russianTextOld.length() / 3, 30);
            return "<s>" + StringUtils.left(russianTextOld, len) + " ... " + StringUtils.right(russianTextOld, len) + "</s>";
        }
        return null;
    }

    public String prepareDiffsReport(List<DiffRow> diffRows) {
        return diffRows.stream()
                .map(d -> DiffService.prepareDiffReport(d))
                .collect(Collectors.joining("\n"));
    }

    public String prepareDiffReport(DiffRow diffRow) {
        return String.format("\tТип: %s.\n" +
                        "\t\tБыло:\n" +
                        "\t\t\t%s\n " +
                        "\t\tстало:\n" +
                        "\t\t\t%s",
                diffRow.getTag(),
                diffRow.getOldLine(),
                diffRow.getNewLine());
    }

    public List<String> prepareInput(String input) {
        return Arrays.asList(input
                .replaceAll("<s>", "")
//                .replaceAll("\n", "<br>")
                .replaceAll("</s>", "")
                .replaceAll("<mark>", "")
                .replaceAll("</mark>", "")
        .split("\n"));
    }
}
