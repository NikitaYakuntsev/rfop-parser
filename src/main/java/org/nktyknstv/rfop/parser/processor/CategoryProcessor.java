package org.nktyknstv.rfop.parser.processor;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.nktyknstv.rfop.parser.entity.BaseEntity;
import org.nktyknstv.rfop.parser.entity.Category;
import org.nktyknstv.rfop.parser.entity.Seminar;
import org.nktyknstv.rfop.parser.repository.SeminarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CategoryProcessor extends BaseProcessor {

    @Resource
    private SeminarRepository seminarRepository;

    @Autowired
    private SeminarProcessor seminarProcessor;

    public static final String BASE_URL = "https://profitcon.ru";

    @Override
    public List<BaseEntity> process(BaseEntity entity) throws Exception {
        Integer pageNum = 1;
        Map<Integer, Elements> seminars = new HashMap<Integer, Elements>();

        while (true) {
            Document page = Jsoup.connect(String.format(entity.getUrl(), pageNum)).get();
            if (!getPageNumber(page).equals(pageNum)) {
                break;
            }
            seminars.put(pageNum, page.select(".table-hover").iterator().next().getElementsByTag("tbody").iterator().next().children());


            pageNum++;
        }


        List<BaseEntity> seminarList = new ArrayList<>();
        for (Elements pageRows : seminars.values()) {
            for (Element row : pageRows) {
                List<Element> columns = row.children().stream().filter(r -> r.childNodes().size() > 0).collect(Collectors.<Element>toList());

                Seminar seminar = new Seminar();
                seminar.setCategory((Category) entity);
                seminar.setCode(getCode(columns));
                seminar.setUrl(getUrl(columns));
                seminar.setName(getName(columns));
                seminar.setCountry(getCountry(columns));
                seminar.setCity(getCity(columns));
                seminar.setDate(getDates(columns));
                seminar.setPriceIn(getPriceIn(columns));
                seminar.setPriceOut(getPriceOut(columns));

                Seminar parsedSeminar = (Seminar) seminarProcessor.process(seminar).iterator().next();
                seminar.setDescription(parsedSeminar.getDescription());
                seminar.setAgenda(parsedSeminar.getAgenda());
                seminar.setSpeakers(parsedSeminar.getSpeakers());

                seminarList.add(seminar);
            }
        }

        return seminarList;

    }

    private Integer getPageNumber(Document page) {
        Integer pageNumber = Integer.valueOf(page.select(".pagination").iterator().next().getElementsByClass("active_el").iterator().next().child(0).childNode(0).toString());
        return pageNumber;
    }

    private String getCode(List<Element> columns) {
        return StringUtils.trimToEmpty(columns.get(0).text());
    }

    private String getUrl(List<Element> columns) {
        return BASE_URL + StringUtils.trimToEmpty(columns.get(1).child(0).attr("href"));
    }

    private String getName(List<Element> columns) {
        return StringUtils.trimToEmpty(columns.get(1).child(0).text());
    }

    private String getCountry(List<Element> columns) {
        return StringUtils.trimToEmpty(columns.get(2).text());
    }

    private String getCity(List<Element> columns) {
        return StringUtils.trimToEmpty(columns.get(3).text());
    }

    private String getDates(List<Element> columns) {
        return StringUtils.trimToEmpty(columns.get(4).childNodes().stream()
                .filter(n -> n instanceof TextNode)
                .map(tn -> ((TextNode) tn).text())
                .collect(Collectors.joining(", ")));
    }

    private String getPriceIn(List<Element> columns) {
        List<Node> nodes = columns.get(5).childNodes();
        try {
            return StringUtils.trimToEmpty(((TextNode) nodes.get(0)).text() + ((TextNode) nodes.get(1).childNode(0)).text());
        } catch (Exception e) {
            System.err.println("Problem with nodes: " + nodes);
            return "";
        }
    }

    private String getPriceOut(List<Element> columns) {
        List<Node> nodes = columns.get(6).childNodes();
        try {
            return StringUtils.trimToEmpty(((TextNode) nodes.get(0)).text() + ((TextNode) nodes.get(1).childNode(0)).text());
        } catch (Exception e) {
            System.err.println("Problem with nodes: " + nodes);
            return "";
        }
    }


}
