package io.github.gourdoni.declension.service;

import java.util.List;

public record LanguageDraft(String title,
                            List<String> cases,
                            List<String> numbers,
                            List<String> genders,
                            List<String> declensions) {}
