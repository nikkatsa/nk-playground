package com.nikoskatsanos.cli;

import com.nikoskatsanos.nkjutils.yalf.YalfLogger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * <p>Simple example using the Apache CLI parser</p>
 *
 * @author nikkatsa
 */
public class PersonalInfoReader {
    private static final YalfLogger log = YalfLogger.getLogger(PersonalInfoReader.class);

    private static final String NAME_CMD_OPTION = "name";
    private static final String SURNAME_CMD_OPTION = "surname";
    private static final String EMAIL_CMD_OPTION = "email";
    private static final String AGE_CMD_OPTION = "age";
    private static final String HELP_CMD_OPTION = "help";

    public static void main(final String... args) {
        try {
            final CommandLine cmd = new DefaultParser().parse(PersonalInfoReader.createCommandLineOptions(), args);

            if (cmd.hasOption(HELP_CMD_OPTION)) {
                new HelpFormatter().printHelp(120, "java", "Person Parsing", PersonalInfoReader.createCommandLineOptions(), "");
                return;
            }

            final String name = cmd.getOptionValue(NAME_CMD_OPTION);
            final String surname = cmd.getOptionValue(SURNAME_CMD_OPTION);
            final int age = Integer.parseInt(cmd.getOptionValue(AGE_CMD_OPTION));
            final String email = cmd.hasOption(EMAIL_CMD_OPTION) ? cmd.getOptionValue(EMAIL_CMD_OPTION) : "<unknown>";

            log.info("%nName: %s%nSurname: %s%nAge:%d%nEmail:%s%n", name, surname, age, email);
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static final Options createCommandLineOptions() {
        final Options options = new Options();

        final Option nameCmdOption = Option.builder("n").hasArg(true).argName("name").longOpt("name").desc("Person's name").required(true).type(String.class).build();
        options.addOption(nameCmdOption);

        final Option surnameCmdOption = Option.builder("s").hasArg(true).argName("surname").longOpt("surname").desc("Person's surname").required(true).type(String.class).build();
        options.addOption(surnameCmdOption);

        final Option ageCmdOption = Option.builder("a").hasArg(true).argName("age").longOpt("age").desc("Person's age").required(true).type(Integer.class).build();
        options.addOption(ageCmdOption);

        final Option emailCmdOption = Option.builder("e").hasArg(true).argName("email").longOpt("email").desc("Person's email").required(false).type(String.class).build();
        options.addOption(emailCmdOption);

        final Option helpCmdOption = Option.builder("h").hasArg(false).argName("help").longOpt("help").desc("Present command line usage information").required(false).type(Boolean.class).build();
        options.addOption(helpCmdOption);

        return options;
    }
}
