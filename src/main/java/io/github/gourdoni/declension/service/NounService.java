package io.github.gourdoni.declension.service;

import io.github.gourdoni.declension.domain.Inflection;
import io.github.gourdoni.declension.domain.InflectionRepository;
import io.github.gourdoni.declension.domain.Noun;
import io.github.gourdoni.declension.domain.NounRepository;

public final class NounService {

    private final NounRepository nouns;
    private final InflectionRepository inflections;

    public NounService(NounRepository nouns, InflectionRepository inflections) {
        this.nouns = nouns;
        this.inflections = inflections;
    }

    public Noun create(NounDraft nounDraft) {
        Noun noun = nouns.save(Noun.of(nounDraft.languageID(), nounDraft.genderID(), nounDraft.declensionID(), nounDraft.gloss()));
        for (NounDraft.InflectionDraft inflection : nounDraft.inflections()) {
            inflections.save(Inflection.of(noun.id(), inflection.caseID(), inflection.noID(), inflection.spelling()));
        }
        return noun;
    }
}
