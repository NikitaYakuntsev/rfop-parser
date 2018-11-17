package org.nktyknstv.rfop.parser.processor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nktyknstv.rfop.parser.entity.BaseEntity;
import org.nktyknstv.rfop.parser.entity.Seminar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SeminarProcessor extends BaseProcessor {
    @Override
    public List<BaseEntity> process(BaseEntity entity) throws Exception {
        Seminar seminar = ((Seminar) entity);

        Document page = Jsoup.connect(entity.getUrl()).get();

        seminar.setDescription(getDescription(page));
        seminar.setAgenda(getAgenda(page));
        seminar.setSpeakers(getSpeakers(page));
        return Collections.singletonList(seminar);
    }


    public String getDescription(Document page) {
        return page.select(".event__programtext").html();
    }

    public String getAgenda(Document page) {
        return page.select(".event__sections").html() + page.select(".event__doptext").html();
    }

    public String getSpeakers(Document page) {
        Elements speakersElements = page.select(".event__speaker-content");
        List<String> speakers = new ArrayList<>();
        for (Element speakerEl : speakersElements) {
            String name = speakerEl.getElementsByClass("event__speakername").text();
            String descr = speakerEl.getElementsByClass("event__speakertitle").text();
            speakers.add(String.format("%s (%s)", name, descr));
        }

        return String.join("; ", speakers);
    }

}
