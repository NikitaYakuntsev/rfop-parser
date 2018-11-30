package org.nktyknstv.rfop.parser.controller;

import org.nktyknstv.rfop.parser.entity.BaseEntity;
import org.nktyknstv.rfop.parser.entity.Category;
import org.nktyknstv.rfop.parser.entity.InspectionResult;
import org.nktyknstv.rfop.parser.entity.Seminar;
import org.nktyknstv.rfop.parser.processor.CategoryProcessor;
import org.nktyknstv.rfop.parser.repository.CategoryRepository;
import org.nktyknstv.rfop.parser.repository.InspectionResultRepository;
import org.nktyknstv.rfop.parser.repository.SeminarRepository;
import org.nktyknstv.rfop.parser.service.InspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class ProcessController {

    @Resource
    private CategoryRepository categoryRepository;
    @Resource
    private SeminarRepository seminarRepository;
    @Resource
    private InspectionResultRepository inspectionResultRepository;

    @Autowired
    private InspectionService inspectionService;


    @Autowired
    private CategoryProcessor categoryProcessor;

    @GetMapping(value = "/init")
    public ResponseEntity<?> init() {
        if (categoryRepository.count() == 0) {
            List<Category> result = new ArrayList<Category>();
            result.add(createCategory("Семинары и курсы", "https://profitcon.ru/events/seminars/?PAGEN_1=%d"));
            result.add(createCategory("Стажировки в России и за рубежом", "https://profitcon.ru/events/foreign-training/?PAGEN_1=%d"));
            result.add(createCategory("Семинары в Сочи, Ялте и Суздале", "https://profitcon.ru/events/seminary-v-sochi-i-yalte/?PAGEN_1=%d"));
        }
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping(value = "/categories/init")
    public ResponseEntity<?> initCategories() throws Exception {
        if (seminarRepository.count() == 0) {
            List<Category> all = categoryRepository.findAll();
            List<BaseEntity> processed = new ArrayList<>();
            for (Category category : all) {
                processed.addAll(categoryProcessor.process(category));
            }
            List<Seminar> seminars = processed.stream()
                    .map(p -> {
                        Seminar seminar = (Seminar) p;
                        seminar.setActual(true);
                        return seminar;
                    })
                    .filter(distinctByKey(s -> s.getUrl().substring(s.getUrl().lastIndexOf('/'))))
                    .collect(Collectors.toList());

            seminarRepository.save(seminars);

        }
        return ResponseEntity.ok(seminarRepository.findAll());
    }

    @GetMapping(value = "/categories/inspect")
    @Transactional
    public ResponseEntity<?> inspectCategories() throws Exception {
        List<Category> all = categoryRepository.findAll();
        List<InspectionResult> inspectionResults = new ArrayList<>();
        List<BaseEntity> processed = new ArrayList<>();
        for (Category category : all) {
            processed.addAll(categoryProcessor.process(category));
        }

//        seminarRepository.setActualFalse();
        List<Seminar> presentSeminars = seminarRepository.findAllByDeletedFalse();
        presentSeminars.forEach(s -> s.setActual(false));
        seminarRepository.save(presentSeminars);
        seminarRepository.flush();

        List<Seminar> allNonActual = seminarRepository.findAll(); //for debug purposes only

        List<Seminar> seminars = processed.stream()
                .map(p -> {
                    Seminar seminar = (Seminar) p;
                    return seminar;
                })
                .filter(distinctByKey(s -> s.getUrl().substring(s.getUrl().lastIndexOf('/'))))
                .collect(Collectors.toList());
        List<Seminar> newSeminars = new ArrayList<>();
        List<Seminar> updatedSeminars = new ArrayList<>();
        List<Seminar> deletedSeminars = new ArrayList<>();
        StringBuilder inspectionDescription = new StringBuilder();
        for (Seminar seminar : seminars) {
            String urlCode = seminar.getUrl().substring(seminar.getUrl().lastIndexOf('/'));
            Optional<Seminar> existingSeminarOpt = seminarRepository.findByCode(urlCode);
            if (!existingSeminarOpt.isPresent()) {
                seminar.setActual(true);
                Seminar newSeminar = seminarRepository.save(seminar);
                newSeminars.add(newSeminar);
            } else {
                Seminar existingSeminar = existingSeminarOpt.get();
                String compareResult = compareSeminars(existingSeminar, seminar);
                if (compareResult.isEmpty()) {
                    existingSeminar.setActual(true);
                    seminarRepository.save(existingSeminar);
                } else {
                    inspectionDescription.append(compareResult).append("\n");
                    existingSeminar.setCode(seminar.getCode());
                    existingSeminar.setName(seminar.getName());
                    existingSeminar.setCountry(seminar.getCountry());
                    existingSeminar.setCity(seminar.getCity());
                    existingSeminar.setDate(seminar.getDate());
                    existingSeminar.setPriceIn(seminar.getPriceIn());
                    existingSeminar.setPriceOut(seminar.getPriceOut());
                    existingSeminar.setDescription(seminar.getDescription());
                    existingSeminar.setAgenda(seminar.getAgenda());
                    existingSeminar.setSpeakers(seminar.getSpeakers());
                    existingSeminar.setActual(true);
                    Seminar updatedSeminar = seminarRepository.save(existingSeminar);
                    updatedSeminars.add(updatedSeminar);
                }
            }
        }
        seminarRepository.flush();
        List<Seminar> allProcessed = seminarRepository.findAll(); //for debug

        List<Seminar> removed = seminarRepository.findAllByActualFalseAndDeletedFalse();
        removed.forEach(r -> r.setActual(true));
        removed.forEach(r -> r.setDeleted(true));
        deletedSeminars.addAll(seminarRepository.save(removed));

        InspectionResult result = new InspectionResult();
        result.setNewEntities(newSeminars.stream().map(BaseEntity::getUrl).collect(Collectors.joining("\n")));
        result.setDeletedEntities(deletedSeminars.stream().map(BaseEntity::getUrl).collect(Collectors.joining("\n")));
        result.setUpdatedEntities(updatedSeminars.stream().map(BaseEntity::getUrl).collect(Collectors.joining("\n")));

        inspectionDescription.append(prepareLinksReport("Добавлен", newSeminars));
        inspectionDescription.append(prepareLinksReport("Удален", deletedSeminars));

        result.setDescription(inspectionDescription.toString());
//        inspectionResults.add();
        return ResponseEntity.ok(inspectionService.saveInspectionResult(result));
    }

    private String compareSeminars(Seminar existingSeminar, Seminar newSeminar) {
        StringBuilder report = new StringBuilder();

        if (!existingSeminar.getDate().equals(newSeminar.getDate())) {
            report.append(String.format("Семинар %s - изменение дат. Было: %s, стало: %s\n", existingSeminar.getUrl(), existingSeminar.getDate(), newSeminar.getDate()));
        }
        if (!existingSeminar.getDescription().equals(newSeminar.getDescription())) {
            report.append(String.format("Семинар %s - изменение описания.\n", existingSeminar.getUrl()));
        }
        if (!existingSeminar.getAgenda().equals(newSeminar.getAgenda())) {
            report.append(String.format("Семинар %s - изменение программы.\n", existingSeminar.getUrl()));
        }
        if (!existingSeminar.getSpeakers().equals(newSeminar.getSpeakers())) {
            report.append(String.format("Семинар %s - изменение лекторов. Было: %s, стало: %s\n", existingSeminar.getUrl(), existingSeminar.getSpeakers(), newSeminar.getSpeakers()));
        }
        return report.toString();
    }

    private String prepareLinksReport(String action, List<Seminar> list) {
        StringBuilder report = new StringBuilder("\n\n");
        for (Seminar seminar : list) {
            report.append(String.format("%s семинар %s (%s)\n", action, seminar.getUrl(), seminar.getName()));
        }
        return report.toString();
    }


    private Category createCategory(String name, String url) {
        Category category = new Category();
        category.setName(name);
        category.setUrl(url);
        return categoryRepository.save(category);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
