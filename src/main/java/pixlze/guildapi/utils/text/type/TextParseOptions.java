package pixlze.guildapi.utils.text.type;

public enum TextParseOptions {
    DEFAULT;

    public String newline;
    public String formatCode;
    public boolean extractUsernames;

    TextParseOptions() {
        newline = "";
        formatCode = "§";
        extractUsernames = false;
    }

    public TextParseOptions withNewline(String newline) {
        this.newline = newline;
        return this;
    }

    public TextParseOptions withFormatCode(String formatCode) {
        this.formatCode = formatCode;
        return this;
    }

    public TextParseOptions withExtractUsernames(boolean extractUsernames) {
        this.extractUsernames = extractUsernames;
        return this;
    }
}
