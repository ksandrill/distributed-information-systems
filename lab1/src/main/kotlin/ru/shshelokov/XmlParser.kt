package ru.shshelokov

import java.io.InputStream
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.StartElement
import kotlin.collections.set


class XmlParser {
    fun parseXml(input: InputStream?): ParseResult {
        val inputFactory = XMLInputFactory.newInstance()
        val reader = inputFactory.createXMLEventReader(input)
        val parentElements = ArrayDeque<StartElement>() /// for nesting elements
        val result = ParseResult()
        while (reader.hasNext()) {
            val nextEvent = reader.nextEvent()
            if (nextEvent.isStartElement) { /// all attributes
                val element = nextEvent.asStartElement()
                if (isNode(element)) {
                    handleNode(element, result)
                }
                if (isTag(element)) {
                    handleTag(element, parentElements, result)
                }
                parentElements.addLast(element)
            }
            if (nextEvent.isEndElement) {
                parentElements.removeLast()
            }
        }
        reader.close()
        return result
    }

    private fun handleNode(element: StartElement, parseResult: ParseResult) {
        val user = element.getAttributeByName(QualifiedNames.user)
        val changeSet = element.getAttributeByName(QualifiedNames.changeSet)
        if (null == user || null == changeSet) {
            return
        }
        parseResult.userChanges.putIfAbsent(user.value, HashSet())
        parseResult.userChanges[user.value]!!.add(changeSet.value)
    }

    private fun handleTag(element: StartElement, parentElements: ArrayDeque<StartElement>, parseResult: ParseResult) {
        val key = element.getAttributeByName(QualifiedNames.key)
        val parent = parentElements.last()
        if (!isNode(parent) || null == key) {
            return
        }
        parseResult.tagUses.putIfAbsent(key.value, 0)
        parseResult.tagUses[key.value] = parseResult.tagUses[key.value]!! + 1
    }

    private fun isNode(element: StartElement): Boolean {
        return element.name == QualifiedNames.node
    }

    private fun isTag(element: StartElement): Boolean {
        return element.name == QualifiedNames.tag
    }

    private object QualifiedNames {
        val key: QName = QName("k")
        val tag: QName = QName("tag")
        val node: QName = QName("node")
        val user: QName = QName("user")
        val changeSet: QName = QName("changeset")
    }

    class ParseResult {
        val userChanges = mutableMapOf<String, MutableSet<String>>() /// user, changeset
        val tagUses = mutableMapOf<String, Int>() /// tag, count  nodes with tags
    }


}
