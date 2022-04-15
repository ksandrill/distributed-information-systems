
import jakarta.xml.bind.JAXBContext
import org.openstreetmap.osm.Node
import java.io.InputStream
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader
import javax.xml.stream.util.StreamReaderDelegate

class XmlParser {
    private val stopTags = setOf("way", "relation")
    private fun skipElements(reader: XMLStreamReader): Boolean {
        while (reader.hasNext()) {
            val eventType = reader.eventType
            if (eventType == XMLStreamConstants.END_DOCUMENT) {
                return false
            }
            if (eventType != XMLStreamConstants.START_ELEMENT) {
                reader.next()
                continue
            }
            val localName = reader.localName
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

    fun parseXml(input: InputStream): ParseResult {
        val inputFactory = XMLInputFactory.newInstance()
        val reader = fixNamespace(inputFactory.createXMLStreamReader(input))
        val context: JAXBContext = JAXBContext.newInstance(Node::class.java)
        val unmarshaller = context.createUnmarshaller()
        val result = ParseResult()
        while (skipElements(reader)) {
            val node: Node = unmarshaller.unmarshal(reader) as Node
            handleNode(node, result)
        }
        reader.close()
        return result
    }

    private fun handleNode(
        node: Node,
        result: ParseResult
    ) {
        result.userChanges.putIfAbsent(node.user, HashSet())
        result.userChanges[node.user]!!.add(node.changeset.longValueExact())
        for (tag in node.tag) {
            result.tagUses.putIfAbsent(tag.k, 0)
            result.tagUses[tag.k] = result.tagUses[tag.k]!! + 1
        }
    }

    class ParseResult {
        val userChanges = HashMap<String, MutableSet<Long>>()
        val tagUses = HashMap<String, Int>()
    }
}