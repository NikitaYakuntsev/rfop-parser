package org.nktyknstv.rfop.parser.service;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class DiffService {



    public String calcDiff(String before, String after) {


        try {
            DiffRowGenerator generator = DiffRowGenerator.create()
                    .ignoreWhiteSpaces(true)
                    .showInlineDiffs(true)
//                    .mergeOriginalRevised(true)
                    .inlineDiffByWord(true)
                    .oldTag(s -> s ? "<s>" : "</s>")
                    .newTag(s -> s ? "<mark>" : "</mark>")
                    .build();
            List<DiffRow> diffRows = generator.generateDiffRows(
                    prepareInput(before),
                    prepareInput(after)
            );
            return diffRows.stream()
                    .map(DiffRow::getNewLine)
                    .collect(Collectors.joining("<br>"));
//                    .replaceAll("&lt;", "<")
//                    .replaceAll("&gt;", ">");
        } catch (DiffException e) {
            return "";
        }
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
        return Arrays.asList(input.replaceAll("<s>", "")
//                .replaceAll("\n", "<br>")
                .replaceAll("</s>", "")
                .replaceAll("<mark>", "")
                .replaceAll("</mark>", ""));
    }
}
