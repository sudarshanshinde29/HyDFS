package org.example.entities;

import java.util.List;

/**
 * This class is the Command object.
 */
public class Command {
    String grep;
    List<Character> optionsList;
    String pattern;
    public Command(String grep, List<Character> optionsList, String pattern) {
        this.grep = grep;
        this.optionsList = optionsList;
        this.pattern = pattern;
    }

    public String getGrep() {
        return grep;
    }

    public void setGrep(String grep) {
        this.grep = grep;
    }

    public List<Character> getOptionsList() {
        return optionsList;
    }

    public void setOptionsList(List<Character> optionsList) {
        this.optionsList = optionsList;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
