# Declension Trainer

A spaced-repetition application specialised for studying and learning noun inflections in highly-inflected Indo-European languages (e.g. Classical Latin).

- [Usage](#usage)
- [Background](#background)
- [Design](#design)

## Usage

### Prerequisites

- JDK 21+
- Maven 3.9+

### Execution

The scripts below build a self-contained `jar` and start the server:

- UNIX: `run.sh`
- Windows: `run.bat`

To do this manually on any platform:

```
mvn clean package
java -jar target/declension-trainer.jar
```

## Background

In an **inflected** language, the shape of nouns changes depending on the role it plays in a sentence.
There are vestiges of this in English, e.g. _horse_ is singular, but _horses_ is plural.
The _-s_ suffix indicates that the noun is plural.
These changes are called **inflections**.

There are certain key categories in highly-inflected (Indo-European) languages that underpin this process:

- **Case** reflects the role a noun plays in a sentence
- **Number** distinguishes singular and plural nouns
- **Gender** categorises nouns, often into _masculine_, _feminine_, and occasionally also _neuter_

The common cases are:

- **Nominative**: the subject
- **Genitive**: possession, "of"
- **Dative**: the indirect object, "to" or "for"
- **Accusative**: the direct object
- **Ablative**: means, manner, or separation; "by", "with", "from"
- **Vocative**: direct address

A noun's gender and its **declension** (another category) dictate the suffixes it takes for each combination of case and number.
For example, _rex_ in Latin is a masculine, 3rd declension noun, which is inflected as follows:

| Case         | Singular | Plural  |
|--------------|----------|---------|
| _Nominative_ | rēx      | rēgēs   |
| _Genitive_   | rēgis    | rēgum   |
| _Dative_     | rēgī     | rēgibus |
| _Accusative_ | rēgem    | rēgēs   |
| _Ablative_   | rēge     | rēgibus |
| _Vocative_   | rēx      | rēgēs   |

## Design

The source code is organised in layers:

- **domain** defines entities and repository/query interfaces
- **persistence** contains JDBC/SQLite implementations of those interfaces
- **scheduling** contains interchangeable spaced-repetition strategies
- **service** defines services that manipulate the domain
- **web** is an HTTP/JSON API that serves the static front end

The full relational database design is as follows:

```mermaid
erDiagram
    language {
        integer id PK
        text title UK
        integer head_case_id FK "nullable"
        integer head_no_id FK "nullable"
    }
    noun_case {
        integer id PK
        integer language_id FK
        text title
        integer ordinal
        integer is_optional
    }
    noun_no {
        integer id PK
        integer language_id FK
        text title
        integer ordinal
    }
    noun_gender {
        integer id PK
        integer language_id FK
        text title
    }
    noun_declension {
        integer id PK
        integer language_id FK
        text title
        integer ordinal
    }
    noun {
        integer id PK
        integer language_id FK
        text gloss "nullable"
        integer gender_id FK
        integer declension_id FK
    }
    inflection {
        integer id PK
        integer noun_id FK
        integer case_id FK
        integer no_id FK
        text spelling
    }
    revision {
        integer id PK
        integer inflection_id FK "unique, 1:1"
        integer interval_days
        real ease_factor
        integer repetitions
        text due_date "nullable"
    }

    language ||--o{ noun_case : "defines"
    language ||--o{ noun_no : "defines"
    language ||--o{ noun_gender : "defines"
    language ||--o{ noun_declension : "defines"
    language ||--o{ noun : "contains"
    language |o--o| noun_case : "headword case"
    language |o--o| noun_no : "headword number"
    noun }o--|| noun_gender : "has"
    noun }o--|| noun_declension : "has"
    noun ||--o{ inflection : "has"
    noun_case ||--o{ inflection : "realised in"
    noun_no ||--o{ inflection : "realised in"
    inflection ||--o| revision : "scheduled by"
```
