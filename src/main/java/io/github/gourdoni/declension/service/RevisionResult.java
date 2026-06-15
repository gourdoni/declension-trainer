package io.github.gourdoni.declension.service;

import java.time.LocalDate;

public record RevisionResult(long inflectionID, boolean correct, LocalDate nextDue) {}
