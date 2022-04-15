package ru.shshelokov

import org.apache.commons.cli.*

private const val FILE_OPTION = "file"
private const val HELP_OPTION = "help"
private const val CONNECTION_OPTION = "con_str"
private const val SHORT_CONNECTION_OPTION = "c"
private const val SHORT_FILE_OPTION = "f"
private const val SHORT_HELP_OPTION = "h"
private const val USER_OPTION = "user"
private const val SHORT_USER_OPTION = "u"
private const val PASSWORD_OPTION = "password"
private const val SHORT_PASSWORD_OPTION = "p"
private const val BATCH_SIZE_OPTION = "batch"
private const val SHORT_BATCH_SIZE_OPTION = "b"
private const val SHORT_TIME_LIMIT_OPTION = "t"
private const val TIME_LIMIT_OPTION = "time"


private fun makeOptions(): Options {

    val readFileOption = Option.builder(SHORT_FILE_OPTION)
        .hasArg(true)
        .longOpt(FILE_OPTION)
        .desc(" path to the source file")
        .build()
    val connectionOption = Option.builder(SHORT_CONNECTION_OPTION)
        .hasArg(true)
        .option(SHORT_CONNECTION_OPTION)
        .longOpt(CONNECTION_OPTION)
        .desc(" db server url")
        .build()
    val userOption = Option.builder(SHORT_USER_OPTION)
        .hasArg(true)
        .option(SHORT_USER_OPTION)
        .longOpt(USER_OPTION)
        .desc("user_name for connect to db")
        .build()
    val passwordOption = Option.builder(SHORT_PASSWORD_OPTION)
        .hasArg(true)
        .option(SHORT_PASSWORD_OPTION)
        .longOpt(PASSWORD_OPTION)
        .desc("password for connect to db")
        .build()
    val batchOption = Option.builder(SHORT_BATCH_SIZE_OPTION)
        .hasArg(true)
        .option(SHORT_BATCH_SIZE_OPTION)
        .longOpt(BATCH_SIZE_OPTION)
        .desc("batch size for connect to db")
        .build()

    val timeLimitOption = Option.builder(SHORT_TIME_LIMIT_OPTION)
        .hasArg(true)
        .option(SHORT_TIME_LIMIT_OPTION)
        .longOpt(TIME_LIMIT_OPTION)
        .desc("batch size for connect to db")
        .build()

    val helpOption = Option.builder()
        .option(SHORT_HELP_OPTION)
        .longOpt(HELP_OPTION)
        .desc("Print help message.")
        .build()

    val options = Options()
    options.addOption(readFileOption)
    options.addOption(helpOption)
    options.addOption(connectionOption)
    options.addOption(userOption)
    options.addOption(passwordOption)
    options.addOption(batchOption)
    options.addOption(timeLimitOption)

    return options
}

class CliOptions(args: Array<String>) {
    private val cli: CommandLine
    private val options: Options = makeOptions()

    init {
        cli = DefaultParser().parse(options, args, true)
    }

    val help
        get() = cli.hasOption(HELP_OPTION)
    val fileName: String
        get() = cli.getOptionValue(FILE_OPTION)

    val username: String
        get() = cli.getOptionValue(USER_OPTION)

    val password: String
        get() = cli.getOptionValue(PASSWORD_OPTION)

    val connectStr: String
        get() = cli.getOptionValue(CONNECTION_OPTION)

    val timeLimitNs: Double
        get() = cli.getOptionValue(TIME_LIMIT_OPTION).toDouble() * 1e9

    val batchSize: Long
        get() = cli.getOptionValue(BATCH_SIZE_OPTION).toLong()


    fun printHelp() =
        HelpFormatter().printHelp("Extract Open Street Map data and load to db with 9 states", options)

}