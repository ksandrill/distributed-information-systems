package ru.shshelokov


import org.apache.commons.cli.MissingOptionException
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.Files
import javax.xml.stream.XMLStreamException
import kotlin.io.path.Path
import kotlin.system.measureTimeMillis


class App

private val logger = logger<App>()


fun getXmlInput(fileName: String): BZip2CompressorInputStream? {
    println(fileName)
    try {
        return BZip2CompressorInputStream(
            BufferedInputStream(
                Files.newInputStream(
                    (Path(fileName)),
                )
            )
        )
    } catch (ex: Exception) {
        when (ex) {
            is IOException, is XMLStreamException -> {
                logger.error(ex.message)
            }
        }

    }
    return null

}


private fun printStatistics(parseResult: XmlParser.ParseResult) {
    println("User changes count:")
    parseResult.userChanges
        .map { it.key to it.value.size }
        .sortedBy { -it.second }
        .forEach { println(it.first + " - " + it.second) }
    println()
    println("Unique tags used - " + parseResult.tagUses.size)
    println()
    println("Tagged nodes count:")
    parseResult.tagUses.forEach { (tag, uses) -> println("$tag - $uses") }
}


fun main(args: Array<String>) {
    val cliOptions = CliOptions(args)
    try {
        if (cliOptions.help) {
            cliOptions.printHelp()
            return
        }
        val time = measureTimeMillis {
            val xmlInput = getXmlInput(cliOptions.fileName)
            xmlInput?.let {
                val xmlParser = XmlParser()
                val parseResult = xmlParser.parseXml(xmlInput)
                printStatistics(parseResult)
            }
        }
        println("taken time: $time ms")

    } catch (e: MissingOptionException) {
        println(e.message)
        cliOptions.printHelp()
    }


}