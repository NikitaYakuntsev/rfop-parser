package org.nktyknstv.rfop.parser.processor;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CategoryProcessor extends BaseProcessor {

    @Resource
    private SeminarRepository seminarRepository;

    @Autowired
    private SeminarProcessor seminarProcessor;

    public static final String BASE_URL = "https://profitcon.ru";

    private int processingOffset = 0;


    Map<String, Map<String, Integer>> MAPPING = new ImmutableMap.Builder<String, Map<String, Integer>>()
            .put("Семинары и курсы", new ImmutableMap.Builder<String, Integer>()
                    .put("code", 0)
                    .put("name", 1)
                    .put("country", 2)
                    .put("city", 3)
                    .put("date", 4)
                    .put("priceIn", 5)
                    .put("priceOut", 6)
                    .build()
            )
            .put("Стажировки в России и за рубежом", new ImmutableMap.Builder<String, Integer>()
                    .put("code", 0)
                    .put("name", 1)
                    .put("country", 2)
                    .put("date", 3)
                    .put("priceIn", 4)
                    .build()
            )
            .put("Семинары в Сочи, Ялте и Суздале", new ImmutableMap.Builder<String, Integer>()
                    .put("code", 0)
                    .put("name", 1)
                    .put("country", 2)
                    .put("city", 3)
                    .put("date", 4)
                    .put("priceIn", 5)
                    .put("priceOut", 6)
                    .build())
            .build();

    @Override
    public List<BaseEntity> process(BaseEntity category) throws Exception {
        Integer pageNum = 0;
        Map<Integer, Elements> seminars = new HashMap<Integer, Elements>();

        while (true) {
            String url = category.getUrl();
            if (pageNum == 0) {
                url = url.substring(0, url.lastIndexOf("/"));
            } else {
                url = String.format(category.getUrl(), pageNum);
            }

            Document page = Jsoup.connect(url).get();
            if (!getPageNumber(page).equals(pageNum) && pageNum != 0) {
                break;
            }
            seminars.put(pageNum, page.select(".table-hover").iterator().next().getElementsByTag("tbody").iterator().next().children());


            pageNum++;
        }


        List<BaseEntity> seminarList = new ArrayList<>();
        for (Elements pageRows : seminars.values()) {
            for (Element row : pageRows) {
                try {
                    processingOffset = 0;
                    List<Element> columns = row.children().stream().filter(r -> r.childNodes().size() > 0).collect(Collectors.<Element>toList());

                    Seminar seminar = new Seminar();
                    seminar.setCategory((Category) category);


                    Map<String, Integer> categoryMapping = MAPPING.get(category.getName());

                    seminar.setCode(getCode(columns, categoryMapping.get("code")));
                    //code is no longer than 7 not but let it be 10
                    if (columns.size() != 7 && seminar.getCode().length() > 10) {
                        log.warn("Problems with row, possibly Code is empty, shifting. Page: {}, Row: {}", pageNum, columns);
                        processingOffset = 1;
                    }

                    seminar.setUrl(getUrl(columns, categoryMapping.get("name")));

                    seminar.setName(getName(columns, categoryMapping.get("name")));

                    seminar.setCountry(getCountry(columns, categoryMapping.get("country")));

                    if (categoryMapping.containsKey("city")) {
                        seminar.setCity(getCity(columns, categoryMapping.get("city")));
                    }

                    seminar.setDate(getDates(columns, categoryMapping.get("date")));

                    seminar.setPriceIn(getPriceIn(columns, categoryMapping.get("priceIn")));

                    if (categoryMapping.containsKey("priceOut")) {
                        seminar.setPriceOut(getPriceOut(columns, categoryMapping.get("priceOut")));
                    }

                    Seminar parsedSeminar = (Seminar) seminarProcessor.process(seminar).iterator().next();
                    seminar.setDescription(parsedSeminar.getDescription());
                    seminar.setAgenda(parsedSeminar.getAgenda());
                    seminar.setSpeakers(parsedSeminar.getSpeakers());

                    seminarList.add(seminar);
                } catch (Exception e) {
                    log.error("Unknown problem occured:", e);
                    continue;
                }
            }
        }

        return seminarList;

    }

    private Integer getPageNumber(Document page) {
        Integer pageNumber = Integer.valueOf(page.select(".pagination").iterator().next().getElementsByClass("active_el").iterator().next().child(0).childNode(0).toString());
        return pageNumber;
    }

    private String getCode(List<Element> columns, Integer index) {
        return StringUtils.trimToEmpty(columns.get(index).text());
    }

    private String getUrl(List<Element> columns, Integer index) {
        try {
            return BASE_URL + StringUtils.trimToEmpty(columns.get(index - processingOffset).child(0).attr("href"));
        } catch (Exception e) {
            log.warn("Problem with fetching seminar url: %s%n (Row: %s%n)", columns.get(1 - processingOffset), columns);
            return "";
        }
    }

    private String getName(List<Element> columns, Integer index) {
        return StringUtils.trimToEmpty(columns.get(index - processingOffset).child(0).text());
    }

    private String getCountry(List<Element> columns, Integer index) {
        return StringUtils.trimToEmpty(columns.get(index - processingOffset).text());
    }

    private String getCity(List<Element> columns, Integer index) {
        return StringUtils.trimToEmpty(columns.get(index - processingOffset).text());
    }

    private String getDates(List<Element> columns, Integer index) {
        return StringUtils.trimToEmpty(columns.get(index - processingOffset).childNodes().stream()
                .filter(n -> n instanceof TextNode)
                .map(tn -> ((TextNode) tn).text())
                .collect(Collectors.joining(", ")));
    }

    private String getPriceIn(List<Element> columns, Integer index) {
        List<Node> nodes = columns.get(index - processingOffset).childNodes();
        try {
            return StringUtils.trimToEmpty(((TextNode) nodes.get(0)).text() + ((TextNode) nodes.get(1).childNode(0)).text());
        } catch (Exception e) {
            log.warn("Problem with PriceIn (is empty): " + columns.get(index - processingOffset));
            return "";
        }
    }

    private String getPriceOut(List<Element> columns, Integer index) {
        List<Node> nodes = columns.get(index - processingOffset).childNodes();
        try {
            return StringUtils.trimToEmpty(((TextNode) nodes.get(0)).text() + ((TextNode) nodes.get(1).childNode(0)).text());
        } catch (Exception e) {
            log.warn("Problem with PriceOut (is empty): " + columns.get(index - processingOffset));
            return "";
        }
    }


}
