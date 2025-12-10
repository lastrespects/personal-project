from pathlib import Path
p = Path(r"src/main/java/com/mmb/service/LearningServiceImpl.java")
text = p.read_text(encoding='utf-8')
start = text.find('private String resolveMeaning')
end = text.find('private String resolveExample')
if start == -1 or end == -1:
    print('markers not found')
    raise SystemExit
new_method = '''    private String resolveMeaning(Word w) {
        String spelling = w.getSpelling() != null ? w.getSpelling() : "";
        String meaning = w.getMeaning();

        boolean hasMeaning = meaning != null && !meaning.isBlank() && !"null".equalsIgnoreCase(meaning);
        if (hasMeaning) {
            String meaningLower = meaning.toLowerCase();
            boolean looksEnglishOnly = meaning.matches("(?i)[a-z\\s]+");
            if (!meaningLower.equals(spelling.toLowerCase()) && !looksEnglishOnly) {
                return meaning;
            }
        }

        String local = LOCAL_MEANINGS.get(spelling.toLowerCase());
        if (local != null && !local.isBlank()) {
            w.setMeaning(local);
            wordRepository.save(w);
            return local;
        }
        try {
            String translated = translationClient.translateToKorean(spelling);
            if (translated != null && !translated.isBlank() && !translated.equalsIgnoreCase(spelling)) {
                w.setMeaning(translated);
                wordRepository.save(w);
                return translated;
            }
        } catch (Exception ignored) {
        }
        String fallback = hasMeaning ? meaning : spelling;
        w.setMeaning(fallback);
        wordRepository.save(w);
        return fallback;
    }

'''
text = text[:start] + new_method + text[end:]
p.write_text(text, encoding='utf-8')
print('method replaced')
