package io.github.gourdoni.declension.service;

import io.github.gourdoni.declension.domain.NounCase;
import io.github.gourdoni.declension.domain.NounDeclension;
import io.github.gourdoni.declension.domain.NounGender;
import io.github.gourdoni.declension.domain.NounNo;

import java.util.List;

public record LanguageCategories(List<NounCase> cases,
                                 List<NounNo> numbers,
                                 List<NounGender> genders,
                                 List<NounDeclension> declensions) {}
