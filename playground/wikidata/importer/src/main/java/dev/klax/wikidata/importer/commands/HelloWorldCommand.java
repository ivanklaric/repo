package dev.klax.wikidata.importer.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class HelloWorldCommand {

    @ShellMethod(key = "hello-world")
    public String helloWorld(
            @ShellOption(defaultValue = "spring") String arg) {
        return "Hello world " + arg;
    }
}
