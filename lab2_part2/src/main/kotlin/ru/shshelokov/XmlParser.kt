package ru.shshelokov

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Unmarshaller
import org.apache.logging.log4j.LogManager
import org.openstreetmap.osm.Node
import java.io.Closeable
import java.io.InputStream
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader
import javax.xml.stream.util.StreamReaderDelegate

class XmlParser(`in`: InputStream) : Closeable, Iterable<Node?>, MutableIterator<Node> {
    private val reader: XMLStreamReader
    private val unmarshaller: Unmarshaller

    init {
        val inputFactory= XMLInputFactory.newInstance()
        reader = fixNamespace(inputFactory.createXMLStreamReader(`in`))
        val context= JAXBContext.newInstance(Node::class.java)
        unmarshaller = context.createUnmarshaller()
    }

    private fun skipElements(): Boolean {
        while (reader.hasNext()) {
            val eventType= reader.eventType
            if (eventType == XMLStreamConstants.END_DOCUMENT) {
                return false
            }
            if (eventType != XMLStreamConstants.START_ELEMENT) {
                reader.next()
                continue
            }
            val localName= reader.localName
            if (stopTags.contains(localName)) {
                return false
            }
            if ("node" == localName) {
                break
            }
            reader.next()
        }
        return true
    }

    override fun close() {
        try {
            reader.close()
        } catch (e: XMLStreamException) {
            log.error(e)
        }
    }

    override fun hasNext(): Boolean {
        return try {
            skipElements()
        } catch (e: XMLStreamException) {
            log.error(e.message)
            throw RuntimeException(e)
        }
    }

    override fun next(): Node {
        return try {
            skipElements()
            unmarshaller.unmarshal(reader) as Node
        } catch (e: XMLStreamException) {
            log.error(e.message)
            throw RuntimeException(e)
        } catch (e: JAXBException) {
            log.error(e.message)
            throw RuntimeException(e)
        }
    }

    override fun iterator(): MutableIterator<Node> {
        return this
    }

    companion object {
        private val log = LogManager.getLogger(
            XmlParser::class.java
        )
        private val stopTags = setOf("way", "relation")
        private fun fixNamespace(reader: XMLStreamReader): XMLStreamReader {
            return object : StreamReaderDelegate(reader) {
                override fun getNamespaceCount(): Int {
                    return 1
                }

                override fun getNamespaceURI(prefix: String): String {
                    return namespaceURI
                }

                override fun getNamespaceURI(index: Int): String {
                    return namespaceURI
                }

                override fun getNamespaceURI(): String {
                    return "http://openstreetmap.org/osm/0.6"
                }

                override fun getNamespacePrefix(index: Int): String {
                    return "osm"
                }
            }
        }
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}