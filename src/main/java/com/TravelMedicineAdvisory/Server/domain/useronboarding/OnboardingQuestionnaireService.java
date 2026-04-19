package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import com.TravelMedicineAdvisory.Server.core.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OnboardingQuestionnaireService {

    private final OnboardingQuestionCategoryRepository questionCategoryRepository;

    public OnboardingQuestionnaireService(OnboardingQuestionCategoryRepository questionCategoryRepository) {
        this.questionCategoryRepository = questionCategoryRepository;
    }

    @Cacheable(cacheNames = CacheNames.ONBOARDING_QUESTIONS)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> loadQuestionnaireStructure() {
        List<OnboardingQuestionCategory> categories = questionCategoryRepository.findAllByOrderByDisplayOrderAsc();
        return categories.stream().map(cat -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", cat.getId());
            m.put("category_key", cat.getCategoryKey());
            m.put("category_name", cat.getCategoryName());
            m.put("category_icon", cat.getCategoryIcon());
            m.put("category_description", cat.getCategoryDescription());
            m.put("display_order", cat.getDisplayOrder());
            m.put("is_optional", cat.getIsOptional());
            m.put("questions", cat.getQuestions());
            return m;
        }).collect(Collectors.toList());
    }
}
