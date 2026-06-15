function app() {
    return {
        languages: [],
        selectedLanguageID: null,
        nouns: [],
        sort: 'due',
        categories: null,
        view: 'nouns',
        error: '',
        langForm: { title: '', cases: [], numbers: [], genders: [], declensions: [] },
        langCounts: { cases: '', numbers: '', genders: '', declensions: '' },
        nounForm: { gloss: '', genderID: '', declensionID: '', spellings: {} },
        review: { queue: [], index: 0, revealed: false, typed: '', done: false },

        init() {
            this.loadLanguages();
        },

        get selectedLanguage() {
            return this.languages.find(l => l.id === this.selectedLanguageID) || null;
        },

        get currentCard() {
            return this.review.queue[this.review.index] || null;
        },

        async api(path, options) {
            const res = await fetch(path, options);
            const data = res.status === 204 ? null : await res.json();
            if (!res.ok) {
                throw new Error((data && data.error) || ('Request failed (' + res.status + ')'));
            }
            return data;
        },

        async loadLanguages() {
            try {
                this.languages = await this.api('/api/languages');
                if (this.selectedLanguageID === null && this.languages.length) {
                    await this.selectLanguage(this.languages[0].id);
                }
            } catch (e) {
                this.error = e.message;
            }
        },

        async selectLanguage(id) {
            this.selectedLanguageID = id;
            this.view = 'nouns';
            this.error = '';
            await Promise.all([this.loadNouns(), this.loadCategories()]);
        },

        async loadNouns() {
            if (this.selectedLanguageID === null) return;
            this.nouns = await this.api(`/api/nouns?language=${this.selectedLanguageID}&sort=${this.sort}`);
        },

        async loadCategories() {
            if (this.selectedLanguageID === null) return;
            this.categories = await this.api(`/api/categories?language=${this.selectedLanguageID}`);
        },

        async setSort(s) {
            this.sort = s;
            await this.loadNouns();
        },

        dueLabel(n) {
            if (n.unseenCount > 0) return 'New';
            if (!n.nextDue) return '—';
            return n.nextDue <= new Date().toISOString().slice(0, 10) ? 'Due now' : n.nextDue;
        },

        openAddLanguage() {
            this.langForm = { title: '', cases: [], numbers: [], genders: [], declensions: [] };
            this.langCounts = { cases: '', numbers: '', genders: '', declensions: '' };
            this.view = 'addLanguage';
            this.error = '';
        },

        async submitLanguage() {
            const nonBlank = rows => rows.filter(r => r.title.trim());
            const titles = rows => nonBlank(rows).map(r => r.title.trim());
            try {
                const created = await this.api('/api/languages', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        title: this.langForm.title.trim(),
                        cases: nonBlank(this.langForm.cases).map(c => ({ title: c.title.trim(), optional: c.optional })),
                        numbers: titles(this.langForm.numbers),
                        genders: titles(this.langForm.genders),
                        declensions: titles(this.langForm.declensions)
                    })
                });
                await this.loadLanguages();
                await this.selectLanguage(created.id);
            } catch (e) {
                this.error = e.message;
            }
        },

        openAddNoun() {
            this.nounForm = { gloss: '', genderID: '', declensionID: '', spellings: {} };
            this.view = 'addNoun';
            this.error = '';
        },

        async submitNoun() {
            if (!this.nounForm.genderID || !this.nounForm.declensionID) {
                this.error = 'Choose a gender and a declension.';
                return;
            }
            const inflections = [];
            for (const c of this.categories.cases) {
                for (const num of this.categories.numbers) {
                    const v = (this.nounForm.spellings[c.id + ':' + num.id] || '').trim();
                    if (v) {
                        inflections.push({ caseID: c.id, noID: num.id, spelling: v });
                    }
                }
            }
            try {
                await this.api('/api/nouns', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        languageID: this.selectedLanguageID,
                        genderID: Number(this.nounForm.genderID),
                        declensionID: Number(this.nounForm.declensionID),
                        gloss: this.nounForm.gloss.trim() || null,
                        inflections
                    })
                });
                this.view = 'nouns';
                await this.loadNouns();
            } catch (e) {
                this.error = e.message;
            }
        },

        setCount(kind, value) {
            const n = Math.max(0, Math.min(30, parseInt(value, 10) || 0));
            const rows = this.langForm[kind];
            while (rows.length < n) {
                rows.push(kind === 'cases' ? { title: '', optional: false } : { title: '' });
            }
            rows.length = n;
        },

        async startReview() {
            try {
                const queue = await this.api(`/api/review?language=${this.selectedLanguageID}`);
                this.review = { queue, index: 0, revealed: false, typed: '', done: queue.length === 0 };
                this.view = 'review';
                this.error = '';
            } catch (e) {
                this.error = e.message;
            }
        },

        reveal() {
            this.review.revealed = true;
        },

        async grade(recall) {
            const card = this.currentCard;
            try {
                await this.api('/api/review/submit', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ responses: [{ inflectionID: card.inflectionID, recall }] })
                });
            } catch (e) {
                this.error = e.message;
                return;
            }
            this.review.index++;
            this.review.typed = '';
            this.review.revealed = false;
            if (this.review.index >= this.review.queue.length) {
                this.review.done = true;
            }
        },

        exitReview() {
            this.view = 'nouns';
            this.loadNouns();
        }
    };
}
