package ru.shshelokov


import jakarta.xml.bind.JAXBException
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import ru.shshelokov.database_util.inserters.base.Iinserter
import ru.shshelokov.database_util.inserters.base.InserterFactory
import ru.shshelokov.database_util.inserters.base.InserterFactory.InserterNotImplementedException
import ru.shshelokov.database_util.inserters.enums.InsertMethod
import ru.shshelokov.database_util.inserters.enums.SchemaType
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import javax.xml.stream.XMLStreamException
import kotlin.system.measureTimeMillis

class App

private val log = logger<App>()


fun main(args: Array<String>) {
//    val connectionString = "jdbc:postgresql://localhost/dafaq"
//    val user = "postgres"
//    val password = "1234"
//    val fileName = "RU-NVS.osm.bz2"
//    val batchSize: Long = 100
//    val timeLimit: Double = 5 * 1e9
    val cliOptions = CliOptions(args)
    try {
        if (cliOptions.help) {
            cliOptions.printHelp()
            return
        }
        val time = measureTimeMillis {
            for (schemaType in SchemaType.values()) {
                for (insertMethod in InsertMethod.values()) {
                    makeInserting(
                        cliOptions.connectStr,
                        cliOptions.username,
                        cliOptions.password,
                        cliOptions.fileName,
                        cliOptions.batchSize,
                        insertMethod,
                        schemaType,
                        cliOptions.timeLimitNs
                    )
                }
            }

        }
        println("taken time: $time ms")

    } catch (e: MissingOptionException) {
        println(e.message)
        cliOptions.printHelp()
    }


}


private fun makeInserting(
    connectionString: String,
    user: String,
    password: String,
    fileName: String,
    batchSize: Long,
    insertMethod: InsertMethod,
    schemaType: SchemaType,
    timeLimit: Double,
    nodesCountLimit: Long = Long.MAX_VALUE
) {

    try {
        DriverManager.getConnection(
            connectionString,
            user,
            password
        ).use { connection ->
            try {
                FileInputStream(fileName).use { rawInput ->
                    BZip2CompressorInputStream(
                        BufferedInputStream(
                            rawInput
                        )
                    ).use { xmlInput ->
                        XmlParser(xmlInput).use { unmarshaller ->
                            connection.autoCommit = false
                            val inserterFactory = InserterFactory(batchSize)
                            val inserter = inserterFactory.getInserter(insertMethod, schemaType)
                            val (totalInserted, elapsed, speed) = dataInsert(
                                connection,
                                unmarshaller,
                                inserter
                            ) { startTime: Long -> isItTimeToContinue(startTime, timeLimit, nodesCountLimit) }
                            var msg =
                                "inserted $totalInserted nodes($schemaType, $insertMethod) in ($elapsed s, $speed nodes/s) "
                            if (InsertMethod.BATCH === insertMethod) {
                                msg += "(batch size $batchSize)"
                            }
                            log.info(msg)
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                connection.rollback()
                throw e
            }
        }
    } catch (e: SQLException) {
        log.error(e)
    } catch (e: IOException) {
        log.error(e)
    } catch (e: XMLStreamException) {
        log.error(e)
    } catch (e: JAXBException) {
        log.error(e)
    } catch (e: InserterNotImplementedException) {
        log.error(e)
    }
}


private fun dataInsert(
    connection: Connection,
    parser: XmlParser,
    inserter: Iinserter,
    isItTimeToContinueFunc: (Long) -> (Long) -> Boolean
): Triple<Long, Double, Double> {
    log.debug("clean tables for inserting")
    inserter.clean(connection)
    log.debug("end cleaning tables for insterting")
    val startTime = System.nanoTime()
    log.debug("start inserting data")
    val totalInserted = inserter.insert(
        parser,
        connection,
        isItTimeToContinueFunc(startTime)
    )
    connection.commit()
    log.debug("end inserting data")
    val elapsed = (System.nanoTime() - startTime) / 1e9
    return Triple(totalInserted, elapsed, totalInserted / elapsed)
}


private fun isItTimeToContinue(
    startTimeNs: Long,
    timeLimitNs: Double,
    nodesCountLimit: Long,
): (Long) -> Boolean = { count: Long -> count < nodesCountLimit && System.nanoTime() - startTimeNs < timeLimitNs }

