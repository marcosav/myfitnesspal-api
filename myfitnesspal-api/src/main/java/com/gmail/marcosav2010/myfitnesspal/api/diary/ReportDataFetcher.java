package com.gmail.marcosav2010.myfitnesspal.api.diary;

import com.gmail.marcosav2010.myfitnesspal.api.BaseFetcher;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;

@RequiredArgsConstructor
public class ReportDataFetcher {

    private static final String FULL_DIARY_REPORT = "reports/printable_diary?from=%s&to=%s";

    private final BaseFetcher fetcher;

    private Document document;

    void fetch(Date date) throws IOException {
        document = fetcher.connect(getURLFullReport(date)).get();
    }

    Float[] getWeights() {
        if (document == null)
            throw new IllegalStateException("No document fetched");

        // misspelled id
        Elements weightElements = document.select("#excercise tbody td.last:not(.first):not(*[colspan])");

        return weightElements.stream()
                .map(Element::text)
                .map(Float::parseFloat).toArray(Float[]::new);
    }

    String[] getNotes() {
        if (document == null)
            throw new IllegalStateException("No document fetched");

        Elements noteElements = document.select("table + h4");
        String[] notes = new String[]{"", ""};
        for (Element h4 : noteElements) {
            Element noteElement = h4.nextElementSibling();
            if (noteElement == null || !noteElement.is("p.notes"))
                continue;

            String note = noteElement.text();
            if (h4.text().contains("Food"))
                notes[0] = note;

            else if (h4.text().contains("Exercise"))
                notes[1] = note;
        }

        return notes;
    }

    void reset() {
        document = null;
    }

    private String getURLFullReport(Date date) {
        String d = Diary.DATE_FORMAT.format(date);
        return fetcher.getURL(FULL_DIARY_REPORT, d, d);
    }
}
